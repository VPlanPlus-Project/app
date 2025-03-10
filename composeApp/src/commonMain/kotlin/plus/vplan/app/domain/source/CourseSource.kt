package plus.vplan.app.domain.source

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.repository.CourseRepository

class CourseSource(
    private val courseRepository: CourseRepository
) {
    private val flows = hashMapOf<Int, MutableSharedFlow<CacheState<Course>>>()

    fun getById(id: Int, forceReload: Boolean = false): Flow<CacheState<Course>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Course>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            MainScope().launch {
                courseRepository.getById(id, forceReload).distinctUntilChanged().collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}