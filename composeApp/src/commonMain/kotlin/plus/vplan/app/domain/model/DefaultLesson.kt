package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem

/**
 * @param id The id of the default lesson. If it originates from indiware, it will be prefixed with `sp24.` followed by the indiware group name and the default lesson number separated with a dot, e.g. `sp24.6c.146`
 */
data class DefaultLesson(
    val id: String,
    val subject: String,
    val course: Cacheable<Course>?,
    val teacher: Cacheable<Teacher>?,
    val groups: List<Cacheable<Group>>
) : CachedItem<DefaultLesson> {
    override fun getItemId(): String = this.id

    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<DefaultLesson>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.course is Course.Fetch && course?.isConfigSatisfied(configuration.course, allowLoading) == false) return false
            if (configuration.teacher is Teacher.Fetch && teacher?.isConfigSatisfied(configuration.teacher, allowLoading) == false) return false
            if (configuration.groups is Group.Fetch && groups.any { !it.isConfigSatisfied(configuration.groups, allowLoading) }) return false
        }
        return true
    }

    data class Fetch(
        val course: CacheableItemSource.FetchConfiguration<Course> = Ignore(),
        val teacher: CacheableItemSource.FetchConfiguration<Teacher> = Ignore(),
        val groups: CacheableItemSource.FetchConfiguration<Group> = Ignore()
    ) : CacheableItemSource.FetchConfiguration.Fetch<DefaultLesson>()
}

fun Collection<DefaultLesson>.findByIndiwareId(indiwareId: String): DefaultLesson? {
    return firstOrNull { it.id.matches(Regex("^sp24\\..*\\.$indiwareId\$")) }
}