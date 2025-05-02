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
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.TeacherRepository

class TeacherSource(
    private val teacherRepository: TeacherRepository
) {

    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<Teacher>>>()
    private val cacheItems = hashMapOf<Int, CacheState<Teacher>>()
    fun getById(id: Int, forceReload: Boolean = false): Flow<CacheState<Teacher>> {
        if (forceReload) flows.remove(id)
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Teacher>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                teacherRepository.getById(id, forceReload = forceReload).collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Int): Teacher? {
        return (cacheItems[id] as? CacheState.Done<Teacher>)?.data ?: getById(id).getFirstValue()
    }
}