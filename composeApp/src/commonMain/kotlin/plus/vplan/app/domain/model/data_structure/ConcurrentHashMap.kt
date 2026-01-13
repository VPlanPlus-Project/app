package plus.vplan.app.domain.model.data_structure

/**
 * Lock-free concurrent hash map for better performance under high concurrency.
 * Platform-specific implementations are provided via dependency injection.
 */
interface ConcurrentHashMap<K: Any, V: Any> {
    /**
     * Returns the value for the given key, or computes it using [defaultValue] if not present.
     * This operation is atomic and lock-free on most platforms.
     */
    fun getOrPut(key: K, defaultValue: () -> V): V

    /**
     * Gets the value associated with the key, or null if not present.
     */
    operator fun get(key: K): V?

    /**
     * Removes the key and its value from the map.
     */
    fun remove(key: K): V?

    /**
     * Checks if the map contains the given key.
     */
    fun containsKey(key: K): Boolean

    /**
     * Sets the value for the given key.
     */
    operator fun set(key: K, value: V)
}

/**
 * Factory interface for creating platform-specific ConcurrentHashMap instances.
 * Implement this in platform-specific source sets and inject via DI.
 */
interface ConcurrentHashMapFactory {
    fun <K: Any, V: Any> create(): ConcurrentHashMap<K, V>
}

