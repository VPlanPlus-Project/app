package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.model.Course
import kotlin.uuid.Uuid

interface CourseRepository {
    fun getAll(): Flow<List<Course>>
    fun getByGroup(groupId: Uuid): Flow<List<Course>>
    fun getBySchool(schoolId: Uuid, forceReload: Boolean): Flow<List<Course>>
    fun getById(id: Int, forceReload: Boolean): Flow<CacheStateOld<Course>>
    fun getByIndiwareId(indiwareId: String): Flow<CacheStateOld<Course>>

    suspend fun upsert(course: Course): Course
    suspend fun upsert(courses: List<Course>)
    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)
}