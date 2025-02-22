package plus.vplan.app.domain.source.schulverwalter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.Year
import plus.vplan.app.domain.repository.schulverwalter.YearRepository

class YearSource(
    private val yearRepository: YearRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<Year>>>()

    fun getById(id: Int): Flow<CacheState<Year>> {
        return cache.getOrPut(id) { yearRepository.getById(id, false).distinctUntilChanged() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheState<Year>>> {
        return yearRepository.getAllIds().flatMapLatest { ids ->
            if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(ids.map { getById(it) }) { it.toList() }
        }
    }
}