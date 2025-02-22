package plus.vplan.app.domain.source.schulverwalter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.repository.schulverwalter.IntervalRepository

class IntervalSource(
    private val intervalRepository: IntervalRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<Interval>>>()

    fun getById(id: Int): Flow<CacheState<Interval>> {
        return cache.getOrPut(id) { intervalRepository.getById(id, false) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheState<Interval>>> {
        return intervalRepository.getAllIds().flatMapLatest { ids ->
            if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(ids.map { getById(it) }) { it.toList() }
        }
    }
}