package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SchoolRepository
import kotlin.uuid.Uuid

class SchoolSource(
    private val schoolRepository: SchoolRepository
) {
    private val flows = hashMapOf<Uuid, MutableSharedFlow<CacheState<School>>>()
    fun getById(
        id: Uuid,
    ): Flow<CacheState<School>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<School>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                schoolRepository.getByLocalId(id).collectLatest { flow.tryEmit(it?.let { CacheState.Done(it) } ?: CacheState.NotExisting(id.toHexString())) }
            }
            flow
        }
    }
}