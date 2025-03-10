package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue

/**
 * @param id The id of the subject instance. If it originates from indiware, it will be prefixed with `sp24.` followed by the indiware school id and group name and the subject instance number separated with a dot, e.g. `sp24.10000000.6c.146`
 */
data class SubjectInstance(
    val id: Int,
    val indiwareId: String?,
    val subject: String,
    val course: Int?,
    val teacher: Int?,
    val groups: List<Int>,
    val cachedAt: Instant
) : Item {
    override fun getEntityId(): String = this.id.toString()

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
}

fun Collection<SubjectInstance>.findByIndiwareId(indiwareId: String): SubjectInstance? {
    return firstOrNull { it.indiwareId.orEmpty().matches(Regex("^sp24\\..*\\.$indiwareId\$")) }
}