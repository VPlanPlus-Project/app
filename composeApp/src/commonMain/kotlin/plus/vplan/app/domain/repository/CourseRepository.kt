package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Course

interface CourseRepository {
    fun getByGroup(groupId: Int): Flow<List<Course>>
    fun getBySchool(schoolId: Int): Flow<List<Course>>
    fun getById(id: String): Flow<Course?>

    suspend fun upsert(course: Course): Flow<Course>
    suspend fun upsert(courses: List<Course>)
    suspend fun deleteById(id: String)
    suspend fun deleteById(ids: List<String>)
}