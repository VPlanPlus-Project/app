@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.core.data.group

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.database.dao.GroupDao
import plus.vplan.app.core.database.dao.SchoolDao
import plus.vplan.app.core.database.model.database.DbGroup
import plus.vplan.app.core.database.model.database.DbGroupAlias
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.CreationReason
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.School
import plus.vplan.app.network.vpp.group.GroupApi
import kotlin.time.Clock
import kotlin.uuid.Uuid

class GroupRepositoryImpl(
    private val schoolRepository: SchoolRepository,
    private val groupDao: GroupDao,
    private val schoolDao: SchoolDao,
    private val groupApi: GroupApi,
    private val applicationScope: CoroutineScope,
) : GroupRepository {

    // Cache pro schoolId: wird geteilt, solange ≥1 Subscriber aktiv (+ 5 s grace period)
    private val bySchoolCache = mutableMapOf<Uuid, Flow<List<Group>>>()
    private val allCache: Flow<List<Group>> by lazy {
        groupDao.getAll()
            .map { items -> items.map { it.toModel() } }
            .distinctUntilChanged()
            .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
    }

    override fun getBySchool(school: School): Flow<List<Group>> {
        return combine(school.aliases.map { alias -> schoolDao.getIdByAlias(alias.value, alias.provider, alias.version) }) { it.firstNotNullOf { it } }
            .flatMapLatest { schoolId ->
                bySchoolCache.getOrPut(schoolId) {
                    groupDao.getBySchool(schoolId)
                        .map { items -> items.map { it.toModel() } }
                        .distinctUntilChanged()
                        .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
                }
            }
    }

    override fun getAll(): Flow<List<Group>> = allCache

    override fun getByIds(identifiers: Set<Alias>, forceUpdate: Boolean): Flow<Group?> {
        if (identifiers.isEmpty()) throw IllegalArgumentException("Identifiers cannot be empty")

        return this
            .findLocalIdByIdentifier(identifiers)
            .flatMapLatest { id -> id?.let { groupDao.findById(id).map { it?.toModel() }.distinctUntilChanged() } ?: flowOf(null) }
            .map { group ->
                if (group == null || forceUpdate) {
                    val result = groupApi.getById(identifiers.first()) ?: return@map null

                    // Check if we already know the group by another alias
                    val resultAliases = result.aliases.mapNotNull { it.toModel() }.toSet()

                    val maybeExistingLocalId = findLocalIdByIdentifier(resultAliases).first()
                    if (maybeExistingLocalId != null) {
                        // We already know the group by another alias, so update it
                        val existingGroup = groupDao.findById(maybeExistingLocalId).first()!!
                        val existingAliases = existingGroup.aliases.map { it.toString() }
                        val aliasesToBeAdded = resultAliases.filter { alias -> alias.toString() !in existingAliases }

                        aliasesToBeAdded
                            .map { DbGroupAlias.fromAlias(it, maybeExistingLocalId) }
                            .forEach { groupDao.upsert(it) }

                        return@map groupDao.findById(maybeExistingLocalId).first()?.toModel()
                    } else {
                        val localId = Uuid.random()
                        val school = schoolRepository.getById(
                            Alias(
                                provider = AliasProvider.Vpp,
                                value = result.schoolId.toString(),
                                version = 1
                            )
                        ).first() ?: return@map null
                        groupDao.upsert(
                            group = DbGroup(
                                id = localId,
                                name = result.name,
                                schoolId = school.id,
                                cachedAt = Clock.System.now(),
                                creationReason = CreationReason.Cached
                            ),
                            aliases = resultAliases.map {
                                DbGroupAlias.fromAlias(it, localId)
                            }
                        )

                        return@map groupDao.findById(localId).first()?.toModel()
                    }
                } else group
            }
    }

    private fun findLocalIdByIdentifier(identifiers: Set<Alias>): Flow<Uuid?> {
        return combine(
            identifiers.map { alias ->
                groupDao.getIdByAlias(
                    value = alias.value,
                    provider = alias.provider,
                    version = alias.version
                )
            }
        ) { ids ->
            ids.firstNotNullOfOrNull { it }
        }
    }

    override suspend fun save(group: Group) {
        groupDao.upsert(
            group = DbGroup(
                id = group.id,
                name = group.name,
                schoolId = group.school.id,
                cachedAt = Clock.System.now(),
                creationReason = CreationReason.Persisted
            ),
            aliases = group.aliases.map {
                DbGroupAlias.fromAlias(it, group.id)
            }
        )
    }
}