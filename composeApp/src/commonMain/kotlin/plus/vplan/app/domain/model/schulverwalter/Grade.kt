package plus.vplan.app.domain.model.schulverwalter

import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.Item

data class Grade(
    val id: Int,
    val value: String?,
    val isOptional: Boolean,
    val isSelectedForFinalGrade: Boolean,
    val subjectId: Int,
    val intervalId: Int,
    val teacherId: Int,
    val collectionId: Int,
    val cachedAt: Instant
): Item {
    override fun getEntityId(): String = this.id.toString()
}
