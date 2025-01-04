package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem

data class Group(
    val id: Int,
    val school: Cacheable<School>,
    val name: String
): CachedItem<Group> {
    override fun getItemId(): String = this.id.toString()

    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<Group>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.school is School.Fetch && !school.isConfigSatisfied(configuration.school, allowLoading)) return false
        }
        return true
    }

    data class Fetch(
        val school: CacheableItemSource.FetchConfiguration<School> = Ignore()
    ) : CacheableItemSource.FetchConfiguration.Fetch<Group>()
}