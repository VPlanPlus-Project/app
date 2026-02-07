package plus.vplan.app.domain.model.data_structure

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLock

/**
 * Apple platforms implementation using a synchronized mutable map with NSLock.
 * Provides thread-safety with minimal lock contention for individual operations.
 */
@OptIn(ExperimentalForeignApi::class)
class AppleConcurrentHashMap<K: Any, V: Any> : ConcurrentHashMap<K, V> {
    private val map = mutableMapOf<K, V>()
    private val lock = NSLock()

    override fun getOrPut(key: K, defaultValue: () -> V): V {
        lock.lock()
        try {
            val existing = map[key]
            if (existing != null) {
                return existing
            }
            val newValue = defaultValue()
            map[key] = newValue
            return newValue
        } finally {
            lock.unlock()
        }
    }

    override operator fun get(key: K): V? {
        lock.lock()
        try {
            return map[key]
        } finally {
            lock.unlock()
        }
    }

    override fun remove(key: K): V? {
        lock.lock()
        try {
            return map.remove(key)
        } finally {
            lock.unlock()
        }
    }

    override fun containsKey(key: K): Boolean {
        lock.lock()
        try {
            return map.containsKey(key)
        } finally {
            lock.unlock()
        }
    }

    override operator fun set(key: K, value: V) {
        lock.lock()
        try {
            map[key] = value
        } finally {
            lock.unlock()
        }
    }
}

/**
 * Factory for creating Apple ConcurrentHashMap instances.
 */
class AppleConcurrentHashMapFactory : ConcurrentHashMapFactory {
    override fun <K: Any, V: Any> create(): ConcurrentHashMap<K, V> {
        return AppleConcurrentHashMap()
    }
}

