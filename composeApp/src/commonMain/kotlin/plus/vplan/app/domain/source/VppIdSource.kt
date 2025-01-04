package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class VppIdSource(
    private val vppIdRepository: VppIdRepository
): CacheableItemSource<VppId> {
    override fun getAll(configuration: CacheableItemSource.FetchConfiguration<VppId>): Flow<List<Cacheable<VppId>>> {
        TODO("Not yet implemented")
    }

    override fun getById(id: String, configuration: CacheableItemSource.FetchConfiguration<VppId>): Flow<Cacheable<VppId>> = vppIdRepository.getVppIdById(id.toInt())
}