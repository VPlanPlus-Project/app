package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class VppIdSource(
    private val vppIdRepository: VppIdRepository
) {
    val cache = hashMapOf<Int, Flow<CacheState<VppId>>>()
    val cacheItems = hashMapOf<Int, CacheState<VppId>>()
    fun getById(id: Int): Flow<CacheState<VppId>> {
        return cache.getOrPut(id) { vppIdRepository.getVppIdById(id) }
    }

    suspend fun getSingleById(id: Int): VppId {
        return (cacheItems[id] as? CacheState.Done<VppId>)?.data ?: getById(id).getFirstValue()
    }
}