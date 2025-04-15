package plus.vplan.app.domain.model.schulverwalter

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item

data class Grade(
    val id: Int,
    val value: String?,
    val isOptional: Boolean,
    val isSelectedForFinalGrade: Boolean,
    val subjectId: Int,
    val teacherId: Int,
    val givenAt: LocalDate,
    val collectionId: Int,
    val vppIdId: Int,
    val cachedAt: Instant
): Item<DataTag> {
    override fun getEntityId(): String = this.id.toString()
    override val tags: Set<DataTag> = emptySet()

    val collection by lazy { App.collectionSource.getById(collectionId) }
    val subject by lazy { App.subjectSource.getById(subjectId) }
    val teacher by lazy { App.schulverwalterTeacherSource.getById(teacherId) }
    val vppId by lazy { App.vppIdSource.getById(vppIdId) }

    val numericValue: Int?
        get() {
            return this.value?.replace("(", "")?.replace(")", "")?.replace("+", "")?.replace("-", "")?.toIntOrNull()
        }
}
