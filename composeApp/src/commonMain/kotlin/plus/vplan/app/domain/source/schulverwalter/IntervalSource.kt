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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.repository.schulverwalter.IntervalRepository

class IntervalSource(
    private val intervalRepository: IntervalRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<Interval>>>()

    fun getById(id: Int): Flow<CacheState<Interval>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Interval>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                intervalRepository.getById(id, false).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheState<Interval>>> {
        return intervalRepository.getAllIds().flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf(emptyList())
            else combine(ids.map { getById(it) }) { it.toList() }
        }
    }

    fun getForUser(schulverwalterUserId: Int): Flow<List<Interval>> {
        return getAll()
            .map { value ->
                value
                    .filterIsInstance<CacheState.Done<Interval>>()
                    .map { it.data }
                    .filter { schulverwalterUserId in it.linkedWithSchulverwalterUserIds }
            }
    }
}