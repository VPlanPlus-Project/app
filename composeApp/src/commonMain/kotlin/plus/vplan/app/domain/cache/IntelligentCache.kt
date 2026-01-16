package plus.vplan.app.domain.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMap
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMapFactory
import kotlin.time.Clock

/**
 * Cache entry with metadata for intelligent eviction
 */
private data class CacheEntry<T>(
    val data: T,
    val cachedAt: Instant,
    var lastAccessedAt: Instant,
    var accessCount: Int = 1
)

/**
 * Intelligent cache implementation with configurable eviction policies and TTL.
 * Provides better memory management and performance compared to indefinite caching.
 */
class IntelligentCache<K, V>(
    private val config: CacheConfig,
    private val concurrentHashMapFactory: ConcurrentHashMapFactory
) {
    private val cache: ConcurrentHashMap<K, CacheEntry<V>> = concurrentHashMapFactory.create()
    private val mutex = Mutex()
    private val accessOrderQueue = mutableListOf<K>() // For LRU
    
    /**
     * Gets a value from the cache if it exists and is not stale
     */
    suspend fun get(key: K): V? {
        val entry = cache[key] ?: return null
        
        // Check if entry is stale
        val now = Clock.System.now()
        if ((now - entry.cachedAt).inWholeMilliseconds > config.ttlMillis) {
            invalidate(key)
            return null
        }
        
        // Update access metadata
        mutex.withLock {
            entry.lastAccessedAt = now
            entry.accessCount++
            
            // Update access order for LRU
            if (config.evictionPolicy == EvictionPolicy.LRU) {
                accessOrderQueue.remove(key)
                accessOrderQueue.add(key)
            }
        }
        
        return entry.data
    }
    
    /**
     * Puts a value in the cache, evicting entries if necessary
     */
    suspend fun put(key: K, value: V) {
        mutex.withLock {
            val now = Clock.System.now()
            
            // Check if we need to evict
            if (cache.size >= config.maxEntries && !cache.containsKey(key)) {
                evictOne()
            }
            
            // Add or update entry
            cache[key] = CacheEntry(
                data = value,
                cachedAt = now,
                lastAccessedAt = now,
                accessCount = 1
            )
            
            // Track for LRU
            if (config.evictionPolicy == EvictionPolicy.LRU) {
                accessOrderQueue.remove(key)
                accessOrderQueue.add(key)
            }
        }
    }
    
    /**
     * Invalidates a specific cache entry
     */
    suspend fun invalidate(key: K) {
        mutex.withLock {
            cache.remove(key)
            accessOrderQueue.remove(key)
        }
    }
    
    /**
     * Invalidates all entries matching a predicate
     */
    suspend fun invalidateMatching(predicate: (K, V) -> Boolean) {
        mutex.withLock {
            val keysToRemove = mutableListOf<K>()
            cache.forEach { (key, entry) ->
                if (predicate(key, entry.data)) {
                    keysToRemove.add(key)
                }
            }
            keysToRemove.forEach { key ->
                cache.remove(key)
                accessOrderQueue.remove(key)
            }
        }
    }
    
    /**
     * Clears all entries from the cache
     */
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
            accessOrderQueue.clear()
        }
    }
    
    /**
     * Evicts stale entries based on TTL
     */
    suspend fun evictStale() {
        mutex.withLock {
            val now = Clock.System.now()
            val keysToRemove = mutableListOf<K>()
            
            cache.forEach { (key, entry) ->
                if ((now - entry.cachedAt).inWholeMilliseconds > config.ttlMillis) {
                    keysToRemove.add(key)
                }
            }
            
            keysToRemove.forEach { key ->
                cache.remove(key)
                accessOrderQueue.remove(key)
            }
        }
    }
    
    /**
     * Returns the current cache size
     */
    fun size(): Int = cache.size
    
    /**
     * Checks if a key exists in the cache (regardless of staleness)
     */
    fun contains(key: K): Boolean = cache.containsKey(key)
    
    /**
     * Evicts one entry based on the configured eviction policy
     */
    private fun evictOne() {
        when (config.evictionPolicy) {
            EvictionPolicy.LRU -> {
                // Evict least recently used
                if (accessOrderQueue.isNotEmpty()) {
                    val keyToEvict = accessOrderQueue.removeAt(0)
                    cache.remove(keyToEvict)
                }
            }
            EvictionPolicy.LFU -> {
                // Evict least frequently used
                val keyToEvict = cache.entries.minByOrNull { it.value.accessCount }?.key
                if (keyToEvict != null) {
                    cache.remove(keyToEvict)
                    accessOrderQueue.remove(keyToEvict)
                }
            }
            EvictionPolicy.FIFO -> {
                // Evict oldest entry
                val keyToEvict = cache.entries.minByOrNull { it.value.cachedAt }?.key
                if (keyToEvict != null) {
                    cache.remove(keyToEvict)
                    accessOrderQueue.remove(keyToEvict)
                }
            }
        }
    }
}
