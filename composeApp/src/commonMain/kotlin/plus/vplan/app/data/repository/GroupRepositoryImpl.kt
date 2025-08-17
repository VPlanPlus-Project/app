package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendEncodedPathSegments
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.currentConfiguration
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupAlias
import plus.vplan.app.data.source.network.model.ApiAlias
import plus.vplan.app.data.source.network.model.IncludedModel
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.GroupDbDto
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.VppGroupDto
import kotlin.uuid.Uuid

class GroupRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : GroupRepository {
    override fun getBySchool(schoolId: Uuid): Flow<List<Group>> {
        return vppDatabase.groupDao.getBySchool(schoolId)
            .map { result -> result.map { it.toModel() } }
    }

    override fun getAllLocalIds(): Flow<List<Uuid>> {
        return vppDatabase.groupDao.getAll().map { it.map { group -> group.group.id } }
    }

    override fun getByLocalId(id: Uuid): Flow<Group?> {
        return vppDatabase.groupDao.findById(id).map { embeddedGroup ->
            embeddedGroup?.toModel()
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

    override suspend fun downloadSchoolIdById(identifier: String): Response<Int> {
        safeRequest(onError = { return  it }) {
            val response = httpClient.get {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("group", "v1", "by-id")
                    appendEncodedPathSegments(identifier)
                }.build())
            }

            val items = response.body<ResponseDataWrapper<UnauthenticatedGroupItemResponse>>()

            return Response.Success(items.data.schoolId)
        }
        return Response.Error.Cancelled
    }

    override suspend fun downloadById(schoolAuthentication: VppSchoolAuthentication, identifier: String): Response<VppGroupDto> {
        safeRequest(onError = { return  it }) {
            val response = httpClient.get {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("group", "v1", "by-id", identifier)
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
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url(URLBuilder(currentConfiguration.apiUrl).apply {
                    appendPathSegments("api", "v2.2", "group", group.id.toString(), "firebase")
                }.build())

                val school = group.school.getFirstValue() as? School.AppSchool
                school?.buildSp24AppAuthentication()?.authentication(this) ?: return Response.Error.Other("No school api access to update firebase token")
                contentType(ContentType.Application.Json)
                setBody(FirebaseTokenRequest(token))
            }
            if (response.status.isSuccess()) return null
            return response.toErrorResponse<Unit>()
        }
        return Response.Error.Cancelled
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

@Serializable
data class UnauthenticatedGroupItemResponse(
    @SerialName("school_id") val schoolId: Int
)