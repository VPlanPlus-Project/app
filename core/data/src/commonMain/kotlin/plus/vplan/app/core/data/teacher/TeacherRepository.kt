package plus.vplan.app.core.data.teacher

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Teacher
import kotlin.uuid.Uuid

interface TeacherRepository {
    fun getById(identifier: Alias) = getByIds(setOf(identifier))
    fun getByIds(identifiers: Set<Alias>): Flow<Teacher?>
    fun getBySchool(school: School): Flow<List<Teacher>>
    fun getAll(): Flow<List<Teacher>>

    @Deprecated("Use aliases")
    fun getByLocalId(id: Uuid): Flow<Teacher?>

    suspend fun save(teacher: Teacher)
}