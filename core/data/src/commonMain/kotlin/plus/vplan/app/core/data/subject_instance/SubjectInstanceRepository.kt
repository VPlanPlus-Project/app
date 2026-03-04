package plus.vplan.app.core.data.subject_instance

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.Teacher
import kotlin.uuid.Uuid

interface SubjectInstanceRepository {
    fun getById(identifier: Alias, forceUpdate: Boolean = false) = getByIds(setOf(identifier), forceUpdate)
    fun getByIds(identifiers: Set<Alias>, forceUpdate: Boolean = false): Flow<SubjectInstance?>
    fun getByGroup(group: Group): Flow<List<SubjectInstance>>
    fun getByTeacher(teacher: Teacher): Flow<List<SubjectInstance>>
    fun getBySchool(school: School): Flow<List<SubjectInstance>>
    fun getAll(): Flow<List<SubjectInstance>>

    @Deprecated("Use alias")
    fun getByLocalId(id: Uuid): Flow<SubjectInstance?>

    suspend fun save(subjectInstance: SubjectInstance)
    suspend fun delete(subjectInstance: SubjectInstance) = delete(listOf(subjectInstance))
    suspend fun delete(subjectInstances: List<SubjectInstance>)
}