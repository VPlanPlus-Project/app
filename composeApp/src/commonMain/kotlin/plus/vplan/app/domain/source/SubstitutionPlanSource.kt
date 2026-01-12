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
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.data_structure.ConcurrentMutableMap
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import kotlin.uuid.Uuid

class SubstitutionPlanSource(
    private val repository: SubstitutionPlanRepository
) {
    private val flows: ConcurrentMutableMap<Uuid, MutableSharedFlow<CacheState<Lesson.SubstitutionPlanLesson>>> = ConcurrentMutableMap()
    fun getById(id: Uuid): Flow<CacheState<Lesson.SubstitutionPlanLesson>> {
        return channelFlow {
            flows.getOrPut(id) {
                val flow = MutableSharedFlow<CacheState<Lesson.SubstitutionPlanLesson>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                CoroutineScope(Dispatchers.IO).launch {
                    repository.getById(id).map { if (it == null) CacheState.NotExisting(id.toHexString()) else CacheState.Done(it) }
                        .collectLatest { flow.tryEmit(it) }
                }
                return@getOrPut flow
            }.collect { send(it) }
        }
    }
}