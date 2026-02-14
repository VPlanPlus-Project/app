@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.currentConfiguration
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupAlias
import plus.vplan.app.data.source.network.GenericAuthenticationProvider
import plus.vplan.app.data.source.network.getAuthenticationOptionsForRestrictedEntity
import plus.vplan.app.data.source.network.model.ApiAlias
import plus.vplan.app.data.source.network.model.IncludedModel
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.domain.model.data_structure.ConcurrentMutableMap
import plus.vplan.app.domain.repository.GroupDbDto
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.VppGroupDto
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.Uuid

class GroupRepositoryImpl(
    private val httpClient: HttpClient,
    private val genericAuthenticationProvider: GenericAuthenticationProvider,
    private val vppDatabase: VppDatabase,
    private val schoolRepository: SchoolRepository
) : GroupRepository {

    override fun getBySchool(schoolId: Uuid): Flow<List<Group>> {
        return vppDatabase.groupDao.getBySchool(schoolId)
            .map { result -> result.map { it.toModel() } }
    }

    private var allLocalIdsFlowCache: Flow<List<Uuid>>? = null
    override fun getAllLocalIds(): Flow<List<Uuid>> {
        allLocalIdsFlowCache?.let { return it }

        val sharedFlow = vppDatabase.groupDao.getAll()
            .map { it.map { group -> group.group.id } }
            .shareIn(
                CoroutineScope(Dispatchers.IO),
                replay = 1,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
            )
            .onCompletion { allLocalIdsFlowCache = null }
        allLocalIdsFlowCache = sharedFlow
        return sharedFlow
    }

    private val localIdFlowCache = mutableMapOf<Uuid, Flow<Group?>>()
    override fun getByLocalId(id: Uuid): Flow<Group?> {
        return localIdFlowCache.getOrPut(id) {
            vppDatabase.groupDao.findById(id).map { embeddedGroup ->
                embeddedGroup?.toModel()
            }
        }
    }

    override suspend fun upsert(item: GroupDbDto): Uuid {
        val resolvedId = resolveAliasesToLocalId(item.aliases)
        val groupId = resolvedId ?: Uuid.random()
        val existing = resolvedId?.let { vppDatabase.groupDao.findById(it) }?.first()
        vppDatabase.groupDao.upsertGroup(
            group = DbGroup(
                id = groupId,
                name = item.name,
                schoolId = item.schoolId,
                cachedAt = Clock.System.now(),
                creationReason = if (existing?.group?.creationReason == CreationReason.Persisted) CreationReason.Persisted else item.creationReason
            ),
            aliases = item.aliases.map {
                DbGroupAlias.fromAlias(it, groupId)
            }
        )
        return groupId
    }

    override suspend fun resolveAliasToLocalId(alias: Alias): Uuid? {
        return vppDatabase.groupDao.getIdByAlias(alias.value, alias.provider, alias.version)
    }

    suspend fun downloadById(schoolAuthentication: VppSchoolAuthentication, identifier: String): Response<VppGroupDto> {
        safeRequest(onError = { return  it }) {
            val response = httpClient.get {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("group", "v1", identifier)
                }.build())

                schoolAuthentication.authentication(this)
            }

            val items = response.body<ResponseDataWrapper<GroupItemResponse>>()

            return Response.Success(items.data.let { groupItemResponse ->
                VppGroupDto(
                    id = groupItemResponse.id,
                    name = groupItemResponse.name,
                    aliases = groupItemResponse.buildAliases()
                )
            })
        }
        return Response.Error.Cancelled
    }

    override suspend fun updateFirebaseToken(group: Group, token: String): Response.Error? {
        return null // TODO
    }

    private val aliasFlowCache = mutableMapOf<String, Flow<AliasState<Group>>>()

    override fun findByAliases(
        aliases: Set<Alias>,
        forceUpdate: Boolean,
        preferCurrentState: Boolean
    ): Flow<AliasState<Group>> {
        val cacheKey = buildString {
            append(aliases.sortedBy { it.toString() }.joinToString(","))
            append("|forceUpdate=$forceUpdate|preferCurrentState=$preferCurrentState")
        }

        if (!forceUpdate) {
            aliasFlowCache[cacheKey]?.let { return it }
        }

        val flow = flow {
            suspend fun emitLocalEntity(forceUpdate: Boolean, preferCurrentState: Boolean): Boolean {
                val localId = resolveAliasesToLocalId(aliases.toList())
                if (localId != null) {
                    if (forceUpdate) {
                        if (!preferCurrentState) {
                            val currentCache = getByLocalId(localId).first()?.let { AliasState.Done(it) }
                            if (currentCache != null) emit(currentCache)
                        }
                    } else {
                        emitAll(getByLocalId(localId).map { if (it == null) AliasState.NotExisting(localId.toHexString()) else AliasState.Done(it) })
                        return true
                    }
                }
                return false
            }

            if (emitLocalEntity(forceUpdate, preferCurrentState)) return@flow

            val downloadError = downloadByAlias(aliases.first())
            if (downloadError != null) {
                emit(AliasState.Error(aliases.first().toString(), downloadError))
                return@flow
            }

            if (!emitLocalEntity(forceUpdate = false, preferCurrentState = false)) {
                emit(AliasState.Error(aliases.first().toString(), Response.Error.Other("Failed to load group with aliases $aliases")))
            }
        }.onCompletion { aliasFlowCache.remove(cacheKey) }
            .shareIn(CoroutineScope(Dispatchers.IO), replay = 1, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000))

        aliasFlowCache[cacheKey] = flow
        return flow
    }

    private val runningDownloads = ConcurrentMutableMap<Alias, Deferred<Response.Error?>>()
    suspend fun downloadByAlias(alias: Alias): Response.Error? {
        runningDownloads[alias]?.let { return it.await() }

        val deferred = CoroutineScope(Dispatchers.IO).async download@{
            try {
                val options = getAuthenticationOptionsForRestrictedEntity(httpClient, URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("group", "v1", alias.toUrlString())
                }.buildString())
                if (options !is Response.Success) return@download options as Response.Error

                val authentication = genericAuthenticationProvider.getAuthentication(options.data)

                if (authentication == null) {
                    return@download Response.Error.Other("No authentication found for school ${options.data}")
                }

                val schoolAliases = options.data.schoolIds.orEmpty()
                    .map { Alias(AliasProvider.Vpp, it.toString(), 1) }
                val localSchoolId = schoolAliases
                    .firstNotNullOfOrNull { schoolRepository.resolveAliasToLocalId(it) }
                if (localSchoolId == null) {
                    return@download Response.Error.Other("No school found for aliases $schoolAliases")
                }

                val item = downloadById(authentication, alias.toUrlString())
                if (item !is Response.Success) return@download item as Response.Error

                upsert(GroupDbDto(
                    schoolId = localSchoolId,
                    name = item.data.name,
                    aliases = item.data.aliases,
                    creationReason = CreationReason.Cached
                ))

                return@download null
            } finally {
                runningDownloads.remove(alias)
            }
        }
        runningDownloads[alias] = deferred
        return deferred.await()
    }
}

@Serializable
data class GroupItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("school") val school: IncludedModel,
    @SerialName("name") val name: String,
    @SerialName("aliases") val aliases: List<ApiAlias>
) {
    fun buildAliases(): List<Alias> = aliases.map { it.toModel() }
}
