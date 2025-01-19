package plus.vplan.app.domain.model

import plus.vplan.app.App
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue

/**
 * @param id The id of the default lesson. If it originates from indiware, it will be prefixed with `sp24.` followed by the indiware group name and the default lesson number separated with a dot, e.g. `sp24.6c.146`
 */
data class DefaultLesson(
    val id: String,
    val subject: String,
    val course: String?,
    val teacher: Int?,
    val groups: List<Int>
) : Item {
    override fun getEntityId(): String = this.id

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

fun Collection<DefaultLesson>.findByIndiwareId(indiwareId: String): DefaultLesson? {
    return firstOrNull { it.id.matches(Regex("^sp24\\..*\\.$indiwareId\$")) }
}