package plus.vplan.app.feature.grades.list.domain.model

import androidx.compose.runtime.Immutable
import plus.vplan.app.feature.grades.common.domain.model.GradeUiItem

@Immutable
data class Subject(
    val id: Int,
    val average: Double?,
    val name: String,
    val categories: List<Category>
) {
    data class Category(
        val id: Int,
        val name: String,
        val average: Double?,
        val count: Int,
        val weight: Double,
        val grades: List<GradeUiItem>,
    )
}