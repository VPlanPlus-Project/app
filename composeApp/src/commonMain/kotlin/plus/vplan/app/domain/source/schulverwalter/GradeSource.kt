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
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.repository.schulverwalter.GradeRepository

class GradeSource(
    private val gradeRepository: GradeRepository
) {

    private val flows = hashMapOf<Int, MutableSharedFlow<CacheStateOld<Grade>>>()

    fun getById(id: Int): Flow<CacheStateOld<Grade>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<Grade>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                gradeRepository.getById(id, false).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheStateOld<Grade>>> {
        return gradeRepository.getAllIds().flatMapLatest { ids ->
            if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(ids.map { getById(it) }) { it.toList() }
        }
    }
}