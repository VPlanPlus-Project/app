package plus.vplan.app.domain.source.schulverwalter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.Subject
import plus.vplan.app.domain.repository.schulverwalter.SubjectRepository

class SubjectSource(
    private val subjectRepository: SubjectRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<Subject>>>()

    fun getById(id: Int): Flow<CacheState<Subject>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Subject>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            MainScope().launch {
                subjectRepository.getById(id, false).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheState<Subject>>> {
        return subjectRepository.getAllIds().flatMapLatest { ids ->
            if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(ids.map { getById(it) }) { it.toList() }
        }
    }
}