package plus.vplan.app.domain.model.data_structure

/**
 * Android implementation using java.util.concurrent.ConcurrentHashMap
 * which provides excellent lock-free performance.
 */
class AndroidConcurrentHashMap<K: Any, V: Any> : ConcurrentHashMap<K, V> {
    private val map = java.util.concurrent.ConcurrentHashMap<K, V>()

    override fun getOrPut(key: K, defaultValue: () -> V): V {
        return map.getOrPut(key, defaultValue)
    }

    override operator fun get(key: K): V? {
        return map[key]
    }

    override fun remove(key: K): V? {
        return map.remove(key)
    }

    override fun containsKey(key: K): Boolean {
        return map.containsKey(key)
    }

    override operator fun set(key: K, value: V) {
        map[key] = value
    }
}

/**
 * Factory for creating Android ConcurrentHashMap instances.
 */
class AndroidConcurrentHashMapFactory : ConcurrentHashMapFactory {
    override fun <K: Any, V: Any> create(): ConcurrentHashMap<K, V> {
        return AndroidConcurrentHashMap()
    }
}
