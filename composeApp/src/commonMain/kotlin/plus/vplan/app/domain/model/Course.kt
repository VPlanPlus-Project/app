package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem

data class Course(
    val id: String,
    val groups: List<Cacheable<Group>>,
    val name: String,
    val teacher: Cacheable<Teacher>?,
): CachedItem<Course> {
    companion object {
        fun fromIndiware(
            sp24SchoolId: String,
            groups: List<Group>,
            name: String,
            teacher: Teacher?
        ) = Course(
            id = "sp24.$sp24SchoolId.$name+${teacher?.name ?: ""}",
            groups = groups.map { Cacheable.Loaded(it) },
            name = name,
            teacher = teacher?.let { Cacheable.Loaded(it) }
        )
    }

    override fun getItemId(): String = this.id

    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<Course>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.groups is Group.Fetch && groups.any { !it.isConfigSatisfied(configuration.groups, allowLoading) }) return false
            if (configuration.teacher is Teacher.Fetch && teacher?.isConfigSatisfied(configuration.teacher, allowLoading) == false) return false
        }
        return true
    }

    data class Fetch(
        val groups: CacheableItemSource.FetchConfiguration<Group> = Ignore(),
        val teacher: CacheableItemSource.FetchConfiguration<Teacher> = Ignore()
    ) : CacheableItemSource.FetchConfiguration.Fetch<Course>()
}