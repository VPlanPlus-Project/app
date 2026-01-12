package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.data_structure.ConcurrentMutableMap
import plus.vplan.app.domain.repository.AssessmentRepository

class AssessmentSource(
    private val assessmentRepository: AssessmentRepository
) {
    private val flows: ConcurrentMutableMap<Int, MutableSharedFlow<CacheState<Assessment>>> = ConcurrentMutableMap()
    fun getById(id: Int, forceUpdate: Boolean = false): Flow<CacheState<Assessment>> {
        if (forceUpdate) kotlinx.coroutines.runBlocking { flows.remove(id) }
        return channelFlow {
            flows.getOrPut(id) {
                val flow = MutableSharedFlow<CacheState<Assessment>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                CoroutineScope(Dispatchers.IO).launch {
                    assessmentRepository.getById(id, forceUpdate).collectLatest { flow.tryEmit(it) }
                }
                return@getOrPut flow
            }.collect { send(it) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheState<Assessment>>> {
        return assessmentRepository.getAll().map { it.map { item -> item.id } }.flatMapLatest {
            if (it.isEmpty()) flowOf(emptyList())
            else combine(it.map { getById(it) }) { it.toList() }
        }
    }
}