package plus.vplan.app.domain.cache

import kotlinx.datetime.Instant
import plus.vplan.app.domain.data.AliasedItem
import plus.vplan.app.domain.data.Item
import kotlin.time.Clock

/**
 * Represents the state of data in a data source with enhanced loading information.
 * Provides granular loading states for linked entities and better refresh control.
 */
sealed class DataSourceState<out T> {
    /**
     * Initial loading state - no data available yet
     */
    data class Loading<T>(val id: String, val linkedEntitiesLoading: Set<String> = emptySet()) : DataSourceState<T>()
    
    /**
     * Data is available with information about linked entity loading states
     */
    data class Success<T>(
        val data: T,
        val linkedEntitiesLoading: Set<String> = emptySet(),
        val isRefreshing: Boolean = false,
        val cachedAt: Instant = Clock.System.now()
    ) : DataSourceState<T>()
    
    /**
     * Data fetch failed
     */
    data class Error<T>(
        val id: String,
        val error: Throwable,
        val cachedData: T? = null
    ) : DataSourceState<T>()
    
    /**
     * Entity does not exist
     */
    data class NotFound<T>(val id: String) : DataSourceState<T>()
}

/**
 * Refresh policy for data sources
 */
enum class RefreshPolicy {
    /**
     * Return cached data immediately, refresh in background if stale
     */
    CACHE_FIRST,
    
    /**
     * Return cached data, always refresh in background
     */
    CACHE_THEN_NETWORK,
    
    /**
     * Wait for fresh data from network, use cache only on error
     */
    NETWORK_FIRST,
    
    /**
     * Force fetch from network, bypass cache entirely
     */
    NETWORK_ONLY,
    
    /**
     * Return only cached data, never fetch from network
     */
    CACHE_ONLY
}

/**
 * Cache invalidation strategy
 */
data class CacheConfig(
    val ttlMillis: Long = 24 * 60 * 60 * 1000L, // 24 hours default in milliseconds
    val maxEntries: Int = 1000,
    val evictionPolicy: EvictionPolicy = EvictionPolicy.LRU
)

enum class EvictionPolicy {
    LRU,  // Least Recently Used
    LFU,  // Least Frequently Used
    FIFO  // First In First Out
}

/**
 * Extension to check if data is stale based on cache config
 */
fun <T> DataSourceState.Success<T>.isStale(config: CacheConfig): Boolean {
    val now = Clock.System.now()
    return (now - cachedAt).inWholeMilliseconds > config.ttlMillis
}

/**
 * Extension to get data if available, null otherwise
 */
fun <T> DataSourceState<T>.getDataOrNull(): T? {
    return when (this) {
        is DataSourceState.Success -> data
        is DataSourceState.Error -> cachedData
        else -> null
    }
}
