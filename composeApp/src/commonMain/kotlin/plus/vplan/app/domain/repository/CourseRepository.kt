package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Course

interface CourseRepository {
    fun getByGroup(groupId: Int): Flow<List<Course>>
    fun getBypId(id: String): Flow<Course?>
    suspend fun upsert(course: Course): Flow<Course>
}