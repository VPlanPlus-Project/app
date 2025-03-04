package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.model.schulverwalter.Subject

class CalculateAverageUseCase {
    suspend operator fun invoke(grades: List<Grade>, interval: Interval, additionalGrades: List<CalculatorGrade> = emptyList()) = invoke(grades.map { CalculatorGrade.ActualGrade(it) } + additionalGrades, interval)

    suspend operator fun invoke(grades: List<CalculatorGrade>, interval: Interval): Double {
        val gradesForInterval = grades.filter {
            val intervalForGrade = when (it) {
                is CalculatorGrade.ActualGrade -> it.grade.collection.getFirstValue()!!.interval.getFirstValue()!!
                else -> interval
            }
            intervalForGrade.id == interval.id || intervalForGrade.includedIntervalId == interval.id
        }

        val gradesBySubject = gradesForInterval.groupBy { it.getSubject() }
        val subjectAverages = mutableListOf<Double>()

        gradesBySubject.forEach { (subject, gradesForSubject) ->

            val categoryAverages = mutableListOf<Double>()

            val gradesForIntervalByType = gradesForSubject.groupBy { it.getType() }

            val categories = gradesForIntervalByType.mapNotNull { (categoryType, gradesForCategory) ->
                val gradesToConsider = gradesForCategory
                    .filter { (it is CalculatorGrade.ActualGrade && it.grade.isSelectedForFinalGrade) || it is CalculatorGrade.CustomGrade }
                    .filter { it.getValue() != null }
                    .mapNotNull { it.getValue() }

                if (gradesToConsider.isEmpty()) return@mapNotNull null

                val rule = subject.finalGrade?.first()?.calculationRule
                val weight = if (rule != null) {
                    val regex = Regex("""([\d.]+)\s*\*\s*\(\(($categoryType)_sum\)/\(($categoryType)_count\)\)""")
                    regex.find(rule)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                } else 1.0

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

        return subjectAverages.average()
    }
}

private data class Category(
    val name: String,
    val sum: Int,
    val count: Int,
    val weight: Double
)

sealed class CalculatorGrade {

    abstract suspend fun getSubject(): Subject
    abstract suspend fun getType(): String
    abstract fun getValue(): Int?

    data class ActualGrade(val grade: Grade): CalculatorGrade() {
        override suspend fun getSubject(): Subject = grade.subject.getFirstValue()!!
        override suspend fun getType(): String = grade.collection.getFirstValue()!!.type
        override fun getValue(): Int? = grade.numericValue
    }
    data class CustomGrade(val grade: Int, val subject: Subject, val type: String): CalculatorGrade() {
        override suspend fun getSubject(): Subject = subject
        override suspend fun getType(): String = type
        override fun getValue(): Int = grade
    }
}