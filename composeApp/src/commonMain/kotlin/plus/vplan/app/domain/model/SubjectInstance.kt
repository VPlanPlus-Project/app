package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasedItem
import kotlin.uuid.Uuid

/**
 * @param id The id of the subject instance. If it originates from indiware, it will be prefixed with `sp24.` followed by the indiware school id and group name and the subject instance number separated with a dot, e.g. `sp24.10000000.6c.146`
 */
data class SubjectInstance(
    override val id: Uuid,
    val subject: String,
    val course: Uuid?,
    val teacher: Uuid?,
    val groups: List<Uuid>,
    val cachedAt: Instant,
    override val aliases: Set<Alias>
) : AliasedItem<DataTag> {
    override val tags: Set<DataTag> = emptySet()

    var courseItem: Course? = null
        private set

    var teacherItem: Teacher? = null
        private set

    var groupItems: List<Group>? = null
        private set

    suspend fun getCourseItem(): Course? {
        if (course == null) return null
        return courseItem ?: App.courseSource.getById(course).getFirstValue().also { courseItem = it }
    }

    suspend fun getTeacherItem(): Teacher? {
        if (this.teacher == null) return null
        return teacherItem ?: App.teacherSource.getById(this.teacher).getFirstValue().also { teacherItem = it }
    }

    suspend fun getGroupItems(): List<Group> {
        return groupItems ?: this.groups.mapNotNull { App.groupSource.getById(it).getFirstValue() }.also { groupItems = it }
    }

    companion object {
        fun buildSp24Alias(sp24SchoolId: Int, sp24VpId: Int): String {
            return ("sp24.$sp24SchoolId/${sp24VpId}")
        }
    }
}