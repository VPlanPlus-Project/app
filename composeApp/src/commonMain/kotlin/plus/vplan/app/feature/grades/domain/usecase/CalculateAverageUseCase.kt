package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.model.schulverwalter.Interval

class CalculateAverageUseCase {
    suspend operator fun invoke(grades: List<Grade>, interval: Interval): Double {
        val gradesForInterval = grades.filter {
            val intervalForGrade = it.collection.getFirstValue()!!.interval.getFirstValue()!!
            intervalForGrade.id == interval.id || intervalForGrade.includedIntervalId == interval.id
        }

        val gradesBySubject = gradesForInterval.groupBy { it.subject.getFirstValue()!! }
        val subjectAverages = mutableListOf<Double>()

        gradesBySubject.forEach { (subject, gradesForSubject) ->

            val categoryAverages = mutableListOf<Double>()

            val gradesForIntervalByType = gradesForSubject.groupBy { it.collection.getFirstValue()!!.type }

            val categories = gradesForIntervalByType.map { (type, gradesForCategory) ->
                val gradesToConsider = gradesForCategory
                    .filter { it.isSelectedForFinalGrade }
                    .filter { it.value?.toDoubleOrNull() != null }

                val rule = subject.finalGrade?.first()?.calculationRule
                val weight = if (rule != null) {
                    val regex = Regex("""([\d.]+)\s*\*\s*\(\(($type)_sum\)/\(($type)_count\)\)""")
                    regex.find(rule)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                } else 1.0

                Category(
                    name = type,
                    sum = gradesToConsider.sumOf { it.value?.toDouble() ?: 0.0 },
                    count = gradesToConsider.size,
                    weight = weight
                )
            }

            categories.forEach { category ->
                categoryAverages.add(category.sum / category.count * (category.weight / categories.sumOf { it.weight }))
            }

            subjectAverages.add(categoryAverages.sum())
        }

        return subjectAverages.average()
    }
}

private data class Category(
    val name: String,
    val sum: Double,
    val count: Int,
    val weight: Double
)