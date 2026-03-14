package plus.vplan.app.feature.grades.common.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.feature.grades.common.domain.model.GradeUiItem

class CalculateAverageUseCase {
    suspend operator fun invoke(grades: List<GradeUiItem>, interval: BesteSchuleInterval): Double {
        return withContext(Dispatchers.Main) {
            val gradesForInterval = grades.filter {
                val intervalForGrade = when (it) {
                    is GradeUiItem.ActualGrade -> it.grade.collection.interval
                    else -> interval
                }
                intervalForGrade.id == interval.id || intervalForGrade.includedIntervalId == interval.id
            }

            val gradesBySubject = gradesForInterval.groupBy { it.getSubjectId() }
            val subjectAverages = mutableListOf<Double>()

            gradesBySubject.forEach { (_, gradesForSubject) ->

                val categoryAverages = mutableListOf<Double>()

                val gradesForIntervalByType = gradesForSubject.groupBy { it.getType() }

                val categories =
                    gradesForIntervalByType.mapNotNull { (categoryType, gradesForCategory) ->
                        val gradesToConsider = gradesForCategory
                            .filter { (it is GradeUiItem.ActualGrade && it.grade.isSelectedForFinalGrade) || it is GradeUiItem.CustomGrade }
                            .filter { it.getValue() != null }
                            .mapNotNull { it.getValue() }

                        if (gradesToConsider.isEmpty()) return@mapNotNull null

                        //fixme
                        val weight = 1.0
                        //                val rule = subject.finalGrade?.first()?.calculationRule
                        //                val weight = if (rule != null) {
                        //                    val regex = Regex("""([\d.]+)\s*\*\s*\(\(($categoryType)_sum\)/\(($categoryType)_count\)\)""")
                        //                    regex.find(rule)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                        //                } else 1.0

                        Category(
                            name = categoryType,
                            sum = gradesToConsider.sum(),
                            count = gradesToConsider.size,
                            weight = weight
                        )
                    }

                categories.forEach { category ->
                    categoryAverages.add(category.sum.toDouble() / category.count * (category.weight / categories.sumOf { it.weight }))
                }

                subjectAverages.add(categoryAverages.sum())
            }

            return@withContext subjectAverages.average()
        }
    }
}

private data class Category(
    val name: String,
    val sum: Int,
    val count: Int,
    val weight: Double
)
