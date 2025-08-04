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
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import kotlin.uuid.Uuid

class SubjectInstanceSource(
    private val subjectInstanceRepository: SubjectInstanceRepository
) {
    private val flows = hashMapOf<Uuid, MutableSharedFlow<CacheState<SubjectInstance>>>()
    private val cacheItems = hashMapOf<Uuid, CacheState<SubjectInstance>>()

    fun getById(id: Uuid): Flow<CacheState<SubjectInstance>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<SubjectInstance>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                subjectInstanceRepository.getByLocalId(id).collectLatest { flow.tryEmit(it?.let { CacheState.Done(it) } ?: CacheState.NotExisting(id.toHexString())) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Uuid): SubjectInstance? {
        return (cacheItems[id] as? CacheState.Done<SubjectInstance>)?.data ?: getById(id).getFirstValue()
    }
}