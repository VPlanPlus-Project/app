package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.repository.RoomRepository

class RoomSource(
    private val roomRepository: RoomRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<Room>>>()
    private val cacheItems = hashMapOf<Int, CacheState<Room>>()
    fun getById(id: Int): Flow<CacheState<Room>> {
        return cache.getOrPut(id) { roomRepository.getById(id, false).onEach { cacheItems[id] = it } }
    }

    suspend fun getSingleById(id: Int): Room? {
        return (cacheItems[id] as? CacheState.Done<Room>)?.data ?: getById(id).getFirstValue()
    }
}