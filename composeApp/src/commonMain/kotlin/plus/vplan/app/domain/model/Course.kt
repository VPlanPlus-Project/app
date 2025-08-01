package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item
import kotlin.uuid.Uuid

data class Course(
    val id: Int,
    val indiwareId: String?,
    val groups: List<Uuid>,
    val name: String,
    val teacherId: Uuid?,
    val cachedAt: Instant
): Item<DataTag> {
    override fun getEntityId(): String = id.toString()
    override val tags: Set<DataTag> = emptySet()

    companion object {
        fun fromIndiware(
            sp24SchoolId: String,
            name: String,
            teacher: Teacher?
        ) = "sp24.$sp24SchoolId.$name+${teacher?.name ?: ""}"
    }

    val teacher by lazy { teacherId?.let { App.teacherSource.getById(it) } }
}