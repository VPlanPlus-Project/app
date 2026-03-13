package plus.vplan.app.feature.grades.domain.usecase

import plus.vplan.app.core.model.besteschule.BesteSchuleGrade
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval

class CalculateAverageUseCase {
    suspend operator fun invoke(grades: List<GradeUiItem>, interval: BesteSchuleInterval): Double {
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

            val categories = gradesForIntervalByType.mapNotNull { (categoryType, gradesForCategory) ->
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

        return subjectAverages.average()
    }
}

private data class Category(
    val name: String,
    val sum: Int,
    val count: Int,
    val weight: Double
)

sealed class GradeUiItem {

    abstract suspend fun getSubjectId(): Int
    abstract suspend fun getType(): String
    abstract fun getValue(): Int?
    abstract val intervalType: BesteSchuleInterval.Type

    data class ActualGrade(val grade: BesteSchuleGrade): GradeUiItem() {
        override suspend fun getSubjectId(): Int = grade.collection.subject.id
        override suspend fun getType(): String = grade.collection.type
        override fun getValue(): Int? = grade.numericValue
        override val intervalType: BesteSchuleInterval.Type = grade.collection.interval.type
    }
    data class CustomGrade(val grade: Int, val subjectId: Int, val type: String, override val intervalType: BesteSchuleInterval.Type): GradeUiItem() {
        override suspend fun getSubjectId(): Int = subjectId
        override suspend fun getType(): String = type
        override fun getValue(): Int = grade
    }
}