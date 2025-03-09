package plus.vplan.app.domain.source

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.SubjectInstanceRepository

class SubjectInstanceSource(
    private val subjectInstanceRepository: SubjectInstanceRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<SubjectInstance>>>()
    private val cacheItems = hashMapOf<Int, CacheState<SubjectInstance>>()

    fun getById(id: Int, forceReload: Boolean = false): Flow<CacheState<SubjectInstance>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<SubjectInstance>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            MainScope().launch {
                subjectInstanceRepository.getById(id, forceReload).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Int): SubjectInstance? {
        return (cacheItems[id] as? CacheState.Done<SubjectInstance>)?.data ?: getById(id).getFirstValue()
    }
}