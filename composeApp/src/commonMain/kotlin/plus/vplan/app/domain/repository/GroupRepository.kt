package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.base.AliasedItemRepository
import kotlin.uuid.Uuid

interface GroupRepository: AliasedItemRepository<GroupDbDto, Group> {
    fun getBySchool(schoolId: Uuid): Flow<List<Group>>

    /**
     * Get the id of the school that this group belongs to. Used to supply the
     * correct authentication for [downloadById].
     */
    suspend fun downloadSchoolIdById(identifier: String): Response<Int>
    suspend fun downloadById(schoolAuthentication: VppSchoolAuthentication, identifier: String): Response<VppGroupDto>

    suspend fun updateFirebaseToken(group: Group, token: String): Response.Error?
}

data class GroupDbDto(
    val schoolId: Uuid,
    val name: String,
    val aliases: List<Alias>,
    val creationReason: CreationReason
)

class VppGroupDto(
    val id: Int,
    val name: String,
    val aliases: List<Alias>
)