package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.base.AliasedItemRepository
import kotlin.uuid.Uuid

interface SubjectInstanceRepository : AliasedItemRepository<SubjectInstanceDbDto, SubjectInstance> {
    fun getByGroup(groupId: Uuid): Flow<List<SubjectInstance>>
    fun getBySchool(schoolId: Uuid): Flow<List<SubjectInstance>>

    /**
     * Get the id of the school that this subject instance belongs to. Used to supply the
     * correct authentication for [downloadById].
     */
    suspend fun downloadSchoolIdById(identifier: String): Response<Int>
    suspend fun downloadById(schoolAuthentication: VppSchoolAuthentication, identifier: String): Response<VppSubjectInstanceDto>

    suspend fun deleteById(id: Uuid)
    suspend fun deleteById(ids: List<Uuid>)
}

data class SubjectInstanceDbDto(
    val subject: String,
    val course: Uuid?,
    val teacher: Uuid?,
    val groups: List<Uuid>,
    val aliases: List<Alias>
)

data class VppSubjectInstanceDto(
    val id: Int,
    val aliases: List<Alias>
)