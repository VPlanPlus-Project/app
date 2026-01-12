package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.model.data_structure.ConcurrentMutableMap
import plus.vplan.app.domain.repository.LessonTimeRepository

class LessonTimeSource(
    private val lessonTimeRepository: LessonTimeRepository
) {
    private val flows: ConcurrentMutableMap<String, MutableSharedFlow<CacheState<LessonTime>>> = ConcurrentMutableMap()
    fun getById(id: String): Flow<CacheState<LessonTime>> {
        return channelFlow {
            flows.getOrPut(id) {
                val flow = MutableSharedFlow<CacheState<LessonTime>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                CoroutineScope(Dispatchers.IO).launch {
                    lessonTimeRepository.getById(id).map { if (it == null) return@map CacheState.NotExisting(id) else CacheState.Done(it) }
                        .collectLatest { flow.tryEmit(it) }
                }
                return@getOrPut flow
            }.collect { send(it) }
        }
    }
}