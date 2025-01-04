package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItem
import plus.vplan.app.domain.cache.CachedItem

data class Teacher(
    val id: Int,
    val school: Cacheable<School>,
    val name: String
) : CachedItem<Teacher> {
    override fun getItemId(): String = this.id.toString()

    override fun isConfigSatisfied(
        configuration: CacheableItem.FetchConfiguration<Teacher>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItem.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.school is School.Fetch && !this.school.isConfigSatisfied(configuration.school, allowLoading)) return false
        }
        return true
    }

    data class Fetch(
        val school: CacheableItem.FetchConfiguration<School> = Ignore()
    ) : CacheableItem.FetchConfiguration.Fetch<Teacher>()
}