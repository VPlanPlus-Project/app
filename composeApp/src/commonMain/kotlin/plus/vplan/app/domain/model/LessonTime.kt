package plus.vplan.app.domain.model

import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItem
import plus.vplan.app.domain.cache.CachedItem

data class LessonTime(
    val id: String,
    val start: LocalTime,
    val end: LocalTime,
    val lessonNumber: Int,
    val group: Cacheable<Group>,
    val interpolated: Boolean = false
) : CachedItem<LessonTime> {
    override fun getItemId(): String = this.id

    override fun isConfigSatisfied(
        configuration: CacheableItem.FetchConfiguration<LessonTime>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItem.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.group is Group.Fetch && !this.group.isConfigSatisfied(configuration.group, allowLoading)) return false
        }
        return true
    }

    data class Fetch(
        val group: CacheableItem.FetchConfiguration<Group> = Ignore()
    ) : CacheableItem.FetchConfiguration.Fetch<LessonTime>()
}
