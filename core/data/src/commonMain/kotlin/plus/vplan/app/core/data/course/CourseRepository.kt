package plus.vplan.app.core.data.course

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Course
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.School
import kotlin.uuid.Uuid

interface CourseRepository {
    fun getById(identifier: Alias) = getByIds(setOf(identifier))
    fun getByIds(identifiers: Set<Alias>): Flow<Course?>
    fun getBySchool(school: School): Flow<List<Course>>
    fun getByGroup(group: Group): Flow<List<Course>>
    fun getAll(): Flow<List<Course>>

    @Deprecated("Use alias")
    fun getByLocalId(id: Uuid): Flow<Course?>

    suspend fun save(course: Course)
    suspend fun delete(course: Course) = delete(listOf(course))
    suspend fun delete(courses: List<Course>)
}