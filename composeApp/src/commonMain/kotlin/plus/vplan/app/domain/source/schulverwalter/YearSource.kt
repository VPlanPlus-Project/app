package plus.vplan.app.domain.source.schulverwalter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.model.schulverwalter.Year
import plus.vplan.app.domain.repository.schulverwalter.YearRepository

class YearSource(
    private val yearRepository: YearRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheStateOld<Year>>>()

    fun getById(id: Int): Flow<CacheStateOld<Year>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<Year>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                yearRepository.getById(id, false).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheStateOld<Year>>> {
        return yearRepository.getAllIds().flatMapLatest { ids ->
            if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(ids.map { getById(it) }) { it.toList() }
        }
    }
}