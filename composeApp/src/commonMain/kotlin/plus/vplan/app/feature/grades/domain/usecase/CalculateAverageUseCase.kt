package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.model.besteschule.BesteSchuleSubject

class CalculateAverageUseCase {
    suspend operator fun invoke(grades: List<BesteSchuleGrade>, interval: BesteSchuleInterval, additionalGrades: List<CalculatorGrade> = emptyList()) = invoke(grades.map { CalculatorGrade.ActualGrade(it) } + additionalGrades, interval)

    suspend operator fun invoke(grades: List<CalculatorGrade>, interval: BesteSchuleInterval): Double? = run {
        val gradesForInterval = grades.filter {
            val intervalForGrade = when (it) {
                is CalculatorGrade.ActualGrade -> it.grade.collection.first()!!.interval.first()!!
                else -> interval
            }
            intervalForGrade.id == interval.id || intervalForGrade.includedIntervalId == interval.id
        }

        val gradesBySubject = gradesForInterval.groupBy { it.getSubject() }
        val subjectAverages = mutableListOf<Double>()

        gradesBySubject.forEach { (_, gradesForSubject) ->

            val categoryAverages = mutableListOf<Double>()

            val gradesForIntervalByType = gradesForSubject.groupBy { it.getType() }

            val categories = gradesForIntervalByType.mapNotNull { (categoryType, gradesForCategory) ->
                val gradesToConsider = gradesForCategory
                    .filter { (it is CalculatorGrade.ActualGrade && it.grade.isSelectedForFinalGrade) || it is CalculatorGrade.CustomGrade }
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

        return@run subjectAverages.average()
    }.let { if (it.isNaN()) return@let null else return@let it }
}

private data class Category(
    val name: String,
    val sum: Int,
    val count: Int,
    val weight: Double
)

sealed class CalculatorGrade {

    abstract suspend fun getSubject(): BesteSchuleSubject
    abstract suspend fun getType(): String
    abstract fun getValue(): Int?

    data class ActualGrade(val grade: BesteSchuleGrade): CalculatorGrade() {
        override suspend fun getSubject(): BesteSchuleSubject = grade.collection.first()!!.subject.first()!!
        override suspend fun getType(): String = grade.collection.first()!!.type
        override fun getValue(): Int? = grade.numericValue
    }
    data class CustomGrade(val grade: Int, val subject: BesteSchuleSubject, val type: String): CalculatorGrade() {
        override suspend fun getSubject(): BesteSchuleSubject = subject
        override suspend fun getType(): String = type
        override fun getValue(): Int = grade
    }
}