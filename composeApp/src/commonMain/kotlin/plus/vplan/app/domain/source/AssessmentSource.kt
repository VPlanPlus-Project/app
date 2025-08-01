package plus.vplan.app.domain.source

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
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.repository.AssessmentRepository

class AssessmentSource(
    private val assessmentRepository: AssessmentRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheStateOld<Assessment>>>()
    fun getById(id: Int, forceUpdate: Boolean = false): Flow<CacheStateOld<Assessment>> {
        if (forceUpdate) flows.remove(id)
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<Assessment>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                assessmentRepository.getById(id, forceUpdate).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheStateOld<Assessment>>> {
        return assessmentRepository.getAll().map { it.map { item -> item.id } }.flatMapLatest {
            return@flatMapLatest if (it.isEmpty()) flowOf(emptyList())
            else combine(it.map { getById(it) }) { it.toList() }
        }
    }
}