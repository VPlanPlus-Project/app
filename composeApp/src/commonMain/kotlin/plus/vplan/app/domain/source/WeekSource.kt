package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.WeekRepository

class WeekSource(
    private val weekRepository: WeekRepository
) {
    private val flows = hashMapOf<String, MutableSharedFlow<CacheState<Week>>>()
    fun getById(id: String): Flow<CacheState<Week>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Week>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                weekRepository.getById(id).map { if (it == null) CacheState.NotExisting(id) else CacheState.Done(it) }
                    .collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}