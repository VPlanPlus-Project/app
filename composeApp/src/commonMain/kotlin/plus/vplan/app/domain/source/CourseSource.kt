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
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.repository.CourseRepository

class CourseSource(
    private val courseRepository: CourseRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheStateOld<Course>>>()

    fun getById(id: Int, forceReload: Boolean = false): Flow<CacheStateOld<Course>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<Course>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                courseRepository.getById(id, forceReload).distinctUntilChanged().collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}