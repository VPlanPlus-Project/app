package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.repository.base.AliasedItemRepository
import kotlin.uuid.Uuid

interface GroupRepository: AliasedItemRepository<GroupDbDto, Group> {
    fun getBySchool(schoolId: Uuid): Flow<List<Group>>

    suspend fun updateFirebaseToken(group: Group, token: String): Response.Error?
}

data class GroupDbDto(
    val schoolId: Uuid,
    val name: String,
    val aliases: List<Alias>
)