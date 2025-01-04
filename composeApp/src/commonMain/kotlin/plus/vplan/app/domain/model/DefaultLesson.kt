package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.CacheableItem
import plus.vplan.app.domain.cache.CachedItem

/**
 * @param id The id of the default lesson. If it originates from indiware, it will be prefixed with `sp24.` followed by the indiware group name and the default lesson number separated with a dot, e.g. `sp24.6c.146`
 */
data class DefaultLesson(
    val id: String,
    val subject: String,
    val course: Course?,
    val teacher: Teacher?,
    val groups: List<Group>
) : CachedItem<DefaultLesson> {
    override fun getItemId(): String = this.id

    override fun isConfigSatisfied(
        configuration: CacheableItem.FetchConfiguration<DefaultLesson>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItem.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.course is Course.Fetch && course?.isConfigSatisfied(configuration.course, allowLoading) == false) return false
            if (configuration.teacher is Teacher.Fetch && teacher?.isConfigSatisfied(configuration.teacher, allowLoading) == false) return false
            if (configuration.groups is Group.Fetch && groups.any { !it.isConfigSatisfied(configuration.groups, allowLoading) }) return false
        }
        return true
    }

    data class Fetch(
        val course: CacheableItem.FetchConfiguration<Course> = Ignore(),
        val teacher: CacheableItem.FetchConfiguration<Teacher> = Ignore(),
        val groups: CacheableItem.FetchConfiguration<Group> = Ignore()
    ) : CacheableItem.FetchConfiguration.Fetch<DefaultLesson>()
}

fun Collection<DefaultLesson>.findByIndiwareId(indiwareId: String): DefaultLesson? {
    return firstOrNull { it.id.matches(Regex("^sp24\\..*\\.$indiwareId\$")) }
}