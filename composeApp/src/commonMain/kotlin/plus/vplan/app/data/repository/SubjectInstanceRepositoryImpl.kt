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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.currentConfiguration
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.DbSubjectInstanceAlias
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.data.source.network.SchoolAuthenticationProvider
import plus.vplan.app.data.source.network.getAuthenticationOptionsForRestrictedEntity
import plus.vplan.app.data.source.network.model.ApiAlias
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.model.data_structure.ConcurrentMutableMap
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.SubjectInstanceDbDto
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import kotlin.uuid.Uuid

class SubjectInstanceRepositoryImpl(
    private val vppDatabase: VppDatabase,
    private val httpClient: HttpClient,
    private val schoolRepository: SchoolRepository,
    private val schoolAuthenticationProvider: SchoolAuthenticationProvider
) : SubjectInstanceRepository {

    private val getByGroupCache = mutableMapOf<Uuid, Flow<List<SubjectInstance>>>()
    override fun getByGroup(groupId: Uuid): Flow<List<SubjectInstance>> {
        return getByGroupCache.getOrPut(groupId) {
            vppDatabase.subjectInstanceDao.getByGroup(groupId).map { it.map { dl -> dl.toModel() } }
        }
    }

    private var allLocalIdsFlowCache: Flow<List<Uuid>>? = null
    override fun getAllLocalIds(): Flow<List<Uuid>> {
        allLocalIdsFlowCache?.let { return it }

        val sharedFlow = vppDatabase.subjectInstanceDao.getAll()
            .map { it.map { subjectInstance -> subjectInstance.subjectInstance.id } }
            .shareIn(
                CoroutineScope(Dispatchers.IO),
                replay = 1,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
            )
            .onCompletion { allLocalIdsFlowCache = null }
        allLocalIdsFlowCache = sharedFlow
        return sharedFlow
    }

    private val getBySchoolCache = mutableMapOf<Uuid, Flow<List<SubjectInstance>>>()
    override fun getBySchool(schoolId: Uuid): Flow<List<SubjectInstance>> {
        return getBySchoolCache.getOrPut(schoolId) {
            vppDatabase.subjectInstanceDao.getBySchool(schoolId).map { it.map { dl -> dl.toModel() } }
        }
    }

    override suspend fun deleteById(id: Uuid) {
        deleteById(listOf(id))
    }

    override suspend fun deleteById(ids: List<Uuid>) {
        vppDatabase.subjectInstanceDao.deleteById(ids)
    }

    private val findByLocalIdCache = mutableMapOf<Uuid, Flow<SubjectInstance?>>()
    override fun getByLocalId(id: Uuid): Flow<SubjectInstance?> {
        return findByLocalIdCache.getOrPut(id) {
            vppDatabase.subjectInstanceDao.findById(id).map { it?.toModel() }
        }
    }

    override suspend fun resolveAliasToLocalId(alias: Alias): Uuid? {
        return vppDatabase.subjectInstanceDao.getIdByAlias(alias.value, alias.provider, alias.version)
    }

    override suspend fun upsert(item: SubjectInstanceDbDto): Uuid {
        val subjectInstanceId = resolveAliasesToLocalId(item.aliases) ?: Uuid.random()
        vppDatabase.subjectInstanceDao.upsertSubjectInstance(
            entity = DbSubjectInstance(
                id = subjectInstanceId,
                subject = item.subject,
                cachedAt = Clock.System.now(),
                teacherId = item.teacher,
                courseId = item.course
            ),
            aliases = item.aliases.map {
                DbSubjectInstanceAlias.fromAlias(it, subjectInstanceId)
            },
            groups = item.groups.map { FKSubjectInstanceGroup(subjectInstanceId, it) }
        )
        return subjectInstanceId
    }

    private suspend fun downloadById(schoolAuthentication: VppSchoolAuthentication, identifier: String): Response<VppSubjectInstanceDto> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("subject-instance", "v1", identifier)
                }.build())

                schoolAuthentication.authentication(this)
            }

            val item = response.body<ResponseDataWrapper<SubjectInstanceVppItemResponse>>().data

            return Response.Success(
                VppSubjectInstanceDto(
                    id = item.id,
                    aliases = item.buildAliases()
                )
            )
        }
        return Response.Error.Cancelled
    }

    private val aliasFlowCache = mutableMapOf<String, Flow<AliasState<SubjectInstance>>>()
    override fun findByAliases(aliases: Set<Alias>, forceUpdate: Boolean, preferCurrentState: Boolean): Flow<AliasState<SubjectInstance>> {
        val cacheKey = buildString {
            append(aliases.sortedBy { it.toString() }.joinToString(","))
            append("|forceUpdate=$forceUpdate|preferCurrentState=$preferCurrentState")
        }

        if (!forceUpdate) {
            aliasFlowCache[cacheKey]?.let { return it }
        }

        val flow = flow {
            val localId = resolveAliasesToLocalId(aliases.toList())
            if (localId != null) {
                if (forceUpdate) {
                    if (!preferCurrentState) {
                        val currentCache = getByLocalId(localId).first()?.let { AliasState.Done(it) }
                        if (currentCache != null) emit(currentCache)
                    }
                } else {
                    emitAll(getByLocalId(localId).map { if (it == null) AliasState.NotExisting(localId.toHexString()) else AliasState.Done(it) })
                    return@flow
                }
            }
            val downloadError = downloadByAlias(aliases.first())
            if (downloadError != null) {
                emit(AliasState.Error(aliases.first().toString(), downloadError))
                return@flow
            }
            emitAll(findByAliases(aliases, forceUpdate = false, preferCurrentState = false))
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
                val vppSchoolIdResponse = getAuthenticationOptionsForRestrictedEntity(httpClient, URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("subject-instance", "v1", alias.toUrlString())
                }.buildString())
                if (vppSchoolIdResponse !is Response.Success) return@download vppSchoolIdResponse as Response.Error

                val authenticationPair = vppSchoolIdResponse.data.schoolIds.orEmpty().firstNotNullOfOrNull { schoolId ->
                    val vppSchoolAlias = Alias(AliasProvider.Vpp, schoolId.toString(), 1)
                    val authentication = schoolAuthenticationProvider.getAuthenticationForSchool(setOf(vppSchoolAlias)) ?: return@firstNotNullOfOrNull null
                    authentication to vppSchoolAlias
                }

                if (authenticationPair == null) {
                    return@download Response.Error.Other("No authentication found for school with id ${vppSchoolIdResponse.data}")
                }

                val (authentication, vppSchoolAlias) = authenticationPair

                val localSchoolId = schoolRepository.resolveAliasToLocalId(vppSchoolAlias)
                if (localSchoolId == null) {
                    return@download Response.Error.Other("No school found for alias $vppSchoolAlias")
                }

                val item = downloadById(authentication, alias.toUrlString())
                if (item !is Response.Success) return@download item as Response.Error

                val existing = resolveAliasesToLocalId(item.data.aliases)?.let {
                    vppDatabase.subjectInstanceDao.findById(it).firstOrNull()?.toModel()
                }

                if (existing == null) {
                    return@download Response.Error.Other("Subject instance with alias $alias is not indexed on vpp server")
                }

                upsert(SubjectInstanceDbDto(
                    subject = existing.subject,
                    course = existing.courseId,
                    teacher = existing.teacher,
                    groups = existing.groupIds,
                    aliases = (existing.aliases + item.data.aliases).distinctBy { it.toString() }
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
data class SubjectInstanceVppItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("aliases") val aliases: List<ApiAlias>
) {
    fun buildAliases(): List<Alias> = aliases.map { it.toModel() }
}

private data class VppSubjectInstanceDto(
    val id: Int,
    val aliases: List<Alias>
)