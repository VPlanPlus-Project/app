package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDateTime
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItem
import plus.vplan.app.domain.cache.CachedItem

sealed class VppId : CachedItem<VppId> {
    abstract val id: Int
    abstract val name: String
    abstract val groups: List<Cacheable<Group>>
    abstract val cachedAt: LocalDateTime

    override fun getItemId(): String = id.toString()
    override fun isConfigSatisfied(
        configuration: CacheableItem.FetchConfiguration<VppId>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItem.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.groups is Group.Fetch && this.groups.any { !it.isConfigSatisfied(configuration.groups, allowLoading) }) return false
        }
        return true
    }

    data class Cached(
        override val id: Int,
        override val name: String,
        override val groups: List<Cacheable<Group>>,
        override val cachedAt: LocalDateTime
    ) : VppId()

    data class Active(
        override val id: Int,
        override val name: String,
        override val groups: List<Cacheable<Group>>,
        override val cachedAt: LocalDateTime,
        val accessToken: String,
        val schulverwalterAccessToken: String?
    ) : VppId()

    override fun hashCode(): Int {
        var result = id.hashCode()
        result += 31 * name.hashCode()
        result += 31 * groups.hashCode()
        result += 31 * cachedAt.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is VppId) return false

        if (this.hashCode() != other.hashCode()) return false

        return true
    }

    data class Fetch(
        val groups: CacheableItem.FetchConfiguration<Group> = Ignore()
    ) : CacheableItem.FetchConfiguration.Fetch<VppId>()
}