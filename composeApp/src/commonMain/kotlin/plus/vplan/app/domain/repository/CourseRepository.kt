package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Course

interface CourseRepository {
    fun getByGroup(groupId: Int): Flow<List<Course>>
    fun getBySchool(schoolId: Int, forceReload: Boolean): Flow<List<Course>>
    fun getById(id: Int): Flow<CacheState<Course>>
    fun getByIndiwareId(indiwareId: String): Flow<CacheState<Course>>

    suspend fun upsert(course: Course): Course
    suspend fun upsert(courses: List<Course>)
    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)
}