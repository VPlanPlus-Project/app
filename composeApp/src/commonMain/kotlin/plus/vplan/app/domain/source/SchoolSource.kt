package plus.vplan.app.domain.source

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SchoolRepository

class SchoolSource(
    private val schoolRepository: SchoolRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<School>>>()
    fun getById(
        id: Int,
    ): Flow<CacheState<School>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<School>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            MainScope().launch {
                schoolRepository.getById(id, false).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}