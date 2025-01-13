package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.repository.CourseRepository

class CourseSource(
    private val courseRepository: CourseRepository
) {
    private val cache = hashMapOf<String, Flow<CacheState<Course>>>()
    fun getById(id: String): Flow<CacheState<Course>> {
        return cache.getOrPut(id) { courseRepository.getById(id).distinctUntilChanged() }
    }
}