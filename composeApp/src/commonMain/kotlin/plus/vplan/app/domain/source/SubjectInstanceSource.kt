package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.SubjectInstanceRepository

class SubjectInstanceSource(
    private val subjectInstanceRepository: SubjectInstanceRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheStateOld<SubjectInstance>>>()
    private val cacheItems = hashMapOf<Int, CacheStateOld<SubjectInstance>>()

    fun getById(id: Int, forceReload: Boolean = false): Flow<CacheStateOld<SubjectInstance>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<SubjectInstance>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                subjectInstanceRepository.getById(id, forceReload).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Int): SubjectInstance? {
        return (cacheItems[id] as? CacheStateOld.Done<SubjectInstance>)?.data ?: getById(id).getFirstValueOld()
    }
}