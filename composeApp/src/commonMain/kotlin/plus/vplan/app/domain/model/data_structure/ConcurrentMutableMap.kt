package plus.vplan.app.domain.model.data_structure

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ConcurrentMutableMap<K, V> {
    private val map = mutableMapOf<K, V>()
    private val mutex = Mutex()

    suspend fun put(key: K, value: V): V? = mutex.withLock {
        map.put(key, value)
    }

    suspend operator fun get(key: K): V? = mutex.withLock {
        map[key]
    }

    suspend fun remove(key: K): V? = mutex.withLock {
        map.remove(key)
    }

    suspend fun containsKey(key: K): Boolean = mutex.withLock {
        map.containsKey(key)
    }

    suspend operator fun set(key: K, value: V) {
        mutex.withLock {
            map[key] = value
        }
    }

    suspend fun getOrPut(key: K, defaultValue: () -> V): V = mutex.withLock {
        map.getOrPut(key, defaultValue)
    }
}
