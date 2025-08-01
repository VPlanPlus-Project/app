package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class VppIdSource(
    private val vppIdRepository: VppIdRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheStateOld<VppId>>>()
    private val cacheItems = hashMapOf<Int, CacheStateOld<VppId>>()
    fun getById(id: Int): Flow<CacheStateOld<VppId>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<VppId>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                vppIdRepository.getById(id, false).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Int): VppId? {
        return (cacheItems[id] as? CacheStateOld.Done<VppId>)?.data ?: getById(id).getFirstValueOld()
    }
}