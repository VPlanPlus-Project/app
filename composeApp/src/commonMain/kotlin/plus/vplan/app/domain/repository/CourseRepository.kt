package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Course

interface CourseRepository {
    fun getAll(): Flow<List<Course>>
    fun getByGroup(groupId: Int): Flow<List<Course>>
    fun getBySchool(schoolId: Int, forceReload: Boolean): Flow<List<Course>>
    fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Course>>
    fun getByIndiwareId(indiwareId: String): Flow<CacheState<Course>>

    suspend fun upsert(course: Course): Course
    suspend fun upsert(courses: List<Course>)
    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)
}