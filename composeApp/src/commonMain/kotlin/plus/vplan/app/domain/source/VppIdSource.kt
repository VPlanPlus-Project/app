package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItem
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class VppIdSource(
    private val vppIdRepository: VppIdRepository
): CacheableItem<VppId> {
    override fun getAll(configuration: CacheableItem.FetchConfiguration<VppId>): Flow<List<VppId>> {
        TODO("Not yet implemented")
    }

    override fun getById(id: Int, configuration: CacheableItem.FetchConfiguration<VppId>): Flow<Cacheable<VppId>> = vppIdRepository.getVppIdById(id)
}