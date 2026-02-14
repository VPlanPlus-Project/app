package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.repository.base.AliasedItemRepository
import kotlin.uuid.Uuid

interface GroupRepository: AliasedItemRepository<GroupDbDto, Group> {
    fun getBySchool(schoolId: Uuid): Flow<List<Group>>

    suspend fun updateFirebaseToken(group: Group, token: String): Response.Error?

    /**
     * @param forceUpdate If true, the item will be fetched from the network even if it exists locally.
     * @param preferCurrentState Only relevant if [forceUpdate] is true. If true, the repository won't emit
     * the cached state while fetching the item from the network. If false, you will first get the cached state
     * (if it exists) and then the updated state.
     */
    fun findByAliases(aliases: Set<Alias>, forceUpdate: Boolean, preferCurrentState: Boolean): Flow<AliasState<Group>>

    /**
     * Calls [findByAliases] with a single alias.
     * @param forceUpdate If true, the item will be fetched from the network even if it exists locally.
     * @param preferCurrentState Only relevant if [forceUpdate] is true. If true, the repository won't emit
     * the cached state while fetching the item from the network. If false, you will first get the cached state
     * (if it exists) and then the updated state.
     * @see findByAliases
     */
    fun findByAlias(alias: Alias, forceUpdate: Boolean, preferCurrentState: Boolean): Flow<AliasState<Group>> {
        return findByAliases(setOf(alias), forceUpdate, preferCurrentState)
    }
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