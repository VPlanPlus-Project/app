package plus.vplan.app.domain.model.schulverwalter

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Item

data class Grade(
    val id: Int,
    val value: String?,
    val isOptional: Boolean,
    val isSelectedForFinalGrade: Boolean,
    val subjectId: Int,
    val teacherId: Int,
    val collectionId: Int,
    val cachedAt: Instant
): Item {
    override fun getEntityId(): String = this.id.toString()

    val collection by lazy { App.collectionSource.getById(collectionId) }
    val subject by lazy { App.subjectSource.getById(subjectId) }
    val teacher by lazy { App.schulverwalterTeacherSource.getById(teacherId) }
}
