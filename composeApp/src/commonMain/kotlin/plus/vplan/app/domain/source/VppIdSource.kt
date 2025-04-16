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
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class VppIdSource(
    private val vppIdRepository: VppIdRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<VppId>>>()
    private val cacheItems = hashMapOf<Int, CacheState<VppId>>()
    fun getById(id: Int): Flow<CacheState<VppId>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<VppId>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                vppIdRepository.getById(id, false).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Int): VppId? {
        return (cacheItems[id] as? CacheState.Done<VppId>)?.data ?: getById(id).getFirstValue()
    }
}