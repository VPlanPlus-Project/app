package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.data.repository.VppSubjectInstanceDto
import plus.vplan.app.domain.repository.base.AliasedItemRepository
import vplanplus.composeapp.generated.resources.Res
import kotlin.uuid.Uuid

interface SubjectInstanceRepository : AliasedItemRepository<SubjectInstanceDbDto, SubjectInstance> {
    fun getByGroup(groupId: Uuid): Flow<List<SubjectInstance>>
    fun getByTeacher(teacherId: Uuid): Flow<List<SubjectInstance>>
    fun getBySchool(schoolId: Uuid): Flow<List<SubjectInstance>>

    suspend fun deleteById(id: Uuid)
    suspend fun deleteById(ids: List<Uuid>)

    fun getByAlias(alias: Alias): Flow<SubjectInstance?> = getByAlias(setOf(alias))
    fun getByAlias(aliases: Collection<Alias>): Flow<SubjectInstance?>

    fun findByAlias(alias: Alias, forceUpdate: Boolean, preferCurrentState: Boolean): Flow<AliasState<SubjectInstance>> {
        return findByAliases(setOf(alias), forceUpdate, preferCurrentState)
    }
    fun findByAliases(aliases: Set<Alias>, forceUpdate: Boolean, preferCurrentState: Boolean): Flow<AliasState<SubjectInstance>>

    suspend fun downloadByAlias(alias: Alias, authentication: VppSchoolAuthentication): Response<VppSubjectInstanceDto>
}

data class SubjectInstanceDbDto(
    val id: Uuid? = null,
    val subject: String,
    val course: Uuid?,
    val teacher: Uuid?,
    val groups: List<Uuid>,
    val aliases: List<Alias>
)