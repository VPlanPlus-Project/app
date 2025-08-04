package plus.vplan.app.domain.source.schulverwalter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.FinalGrade
import plus.vplan.app.domain.repository.schulverwalter.FinalGradeRepository

class FinalGradeSource(
    private val finalGradeRepository: FinalGradeRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<FinalGrade>>>()

    fun getById(id: Int): MutableSharedFlow<CacheState<FinalGrade>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<FinalGrade>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                finalGradeRepository.getById(id, false).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}