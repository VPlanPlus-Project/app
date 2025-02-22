package plus.vplan.app.domain.source.schulverwalter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.Collection
import plus.vplan.app.domain.repository.schulverwalter.CollectionRepository

class CollectionSource(
    private val collectionRepository: CollectionRepository
) {

    private val cache = hashMapOf<Int, Flow<CacheState<Collection>>>()

    fun getById(id: Int): Flow<CacheState<Collection>> {
        return cache.getOrPut(id) { collectionRepository.getById(id, false) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheState<Collection>>> {
        return collectionRepository.getAllIds().flatMapLatest { ids ->
            if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(ids.map { getById(it) }) { it.toList() }
        }
    }
}