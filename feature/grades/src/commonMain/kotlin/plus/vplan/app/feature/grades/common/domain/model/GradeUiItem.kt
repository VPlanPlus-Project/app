package plus.vplan.app.feature.grades.common.domain.model

import plus.vplan.app.core.model.besteschule.BesteSchuleGrade
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval

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