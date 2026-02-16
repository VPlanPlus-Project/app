package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.domain.repository.base.AliasedItemRepository
import kotlin.uuid.Uuid

interface SubjectInstanceRepository : AliasedItemRepository<SubjectInstanceDbDto, SubjectInstance> {
    fun getByGroup(groupId: Uuid): Flow<List<SubjectInstance>>
    fun getByTeacher(teacherId: Uuid): Flow<List<SubjectInstance>>
    fun getBySchool(schoolId: Uuid): Flow<List<SubjectInstance>>

    suspend fun deleteById(id: Uuid)
    suspend fun deleteById(ids: List<Uuid>)

    fun findByAlias(alias: Alias, forceUpdate: Boolean, preferCurrentState: Boolean): Flow<AliasState<SubjectInstance>> {
        return findByAliases(setOf(alias), forceUpdate, preferCurrentState)
    }
    fun findByAliases(aliases: Set<Alias>, forceUpdate: Boolean, preferCurrentState: Boolean): Flow<AliasState<SubjectInstance>>
}

data class SubjectInstanceDbDto(
    val subject: String,
    val course: Uuid?,
    val teacher: Uuid?,
    val groups: List<Uuid>,
    val aliases: List<Alias>
)