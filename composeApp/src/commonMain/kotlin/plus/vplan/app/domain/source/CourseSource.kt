package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.repository.CourseRepository
import kotlin.uuid.Uuid

class CourseSource(
    private val courseRepository: CourseRepository
) {
    private val flows = hashMapOf<Uuid, MutableSharedFlow<CacheState<Course>>>()

    fun getById(id: Uuid): Flow<CacheState<Course>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Course>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                courseRepository.getByLocalId(id).distinctUntilChanged().collectLatest { flow.tryEmit(it?.let { CacheState.Done(it) } ?: CacheState.NotExisting(id.toHexString())) }
            }
            flow
        }
    }
}