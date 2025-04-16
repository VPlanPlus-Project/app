package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.repository.RoomRepository

class RoomSource(
    private val roomRepository: RoomRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<Room>>>()
    private val cacheItems = hashMapOf<Int, CacheState<Room>>()
    fun getById(id: Int, forceReload: Boolean = false): Flow<CacheState<Room>> {
        if (forceReload) flows.remove(id)
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Room>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                roomRepository.getById(id, forceReload).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Int): Room? {
        return (cacheItems[id] as? CacheState.Done<Room>)?.data ?: getById(id).getFirstValue()
    }
}