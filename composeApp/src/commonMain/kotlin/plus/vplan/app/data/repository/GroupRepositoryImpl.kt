package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchoolGroup
import plus.vplan.app.data.source.network.isResponseFromBackend
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.utils.sendAll

class GroupRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : GroupRepository {
    override fun getBySchool(schoolId: Int): Flow<List<Group>> {
        return vppDatabase.groupDao.getBySchool(schoolId)
            .map { result -> result.map { it.toModel() } }
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.groupDao.getAll().map { result -> result.map { it.group.id } }
    }

    override suspend fun getBySchoolWithCaching(school: School, forceReload: Boolean): Response<Flow<List<Group>>> {
        val flow = getBySchool(school.id)
        if (flow.first().isNotEmpty() && !forceReload) return Response.Success(flow)

        safeRequest(onError = { return it }) {
            val response = httpClient.get("${api.url}/api/v2.2/school/${school.id}/group") {
                school.getSchoolApiAccess()?.authentication(this) ?: Response.Error.Other("no auth")
            }
            if (!response.status.isSuccess()) return Response.Error.Other(response.status.toString())
            val data =
                ResponseDataWrapper.fromJson<List<SchoolItemGroupsResponse>>(response.bodyAsText())
                    ?: return Response.Error.ParsingError(response.bodyAsText())
            data.forEach { group ->
                vppDatabase.groupDao.upsert(
                    DbGroup(
                        id = group.id,
                        name = group.name,
                        cachedAt = Clock.System.now()
                    ),
                    FKSchoolGroup(
                        schoolId = school.id,
                        groupId = group.id
                    )
                )
            }
            return Response.Success(getBySchool(school.id))
        }
        return Response.Error.Cancelled
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Group>> {
        val groupFlow = vppDatabase.groupDao.getById(id).map { it?.toModel() }
        return channelFlow {
            if (!forceReload) {
                var hadData = false
                sendAll(groupFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheState.Done(it) })
                if (hadData) return@channelFlow
            }
            send(CacheState.Loading(id.toString()))

            safeRequest(onError = { trySend(CacheState.Error(id, it)) }) {
                val existing = vppDatabase.groupDao.getById(id).first()
                var schoolApiAccess: SchoolApiAccess? = null
                if (existing != null) {
                    schoolApiAccess = vppDatabase.schoolDao.findById(existing.school.schoolId).first()?.toModel()?.getSchoolApiAccess()
                }

                if (schoolApiAccess == null) {
                    val accessResponse = httpClient.get("${api.url}/api/v2.2/group/$id")
                    if (accessResponse.status == HttpStatusCode.NotFound && accessResponse.isResponseFromBackend()) {
                        vppDatabase.groupDao.deleteById(listOf(id))
                        return@channelFlow send(CacheState.NotExisting(id.toString()))
                    }
                    if (!accessResponse.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), accessResponse.toErrorResponse<Group>()))
                    val accessData = ResponseDataWrapper.fromJson<GroupUnauthenticatedResponse>(accessResponse.bodyAsText())
                        ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(accessResponse.bodyAsText())))

                    schoolApiAccess = accessData.schoolId.let {
                        val school = vppDatabase.schoolDao.findById(it).first()?.toModel() ?: return@let null
                        if (school is School.IndiwareSchool && !school.credentialsValid) return@let null
                        return@let school.getSchoolApiAccess()
                    }
                }

                if (schoolApiAccess == null) {
                    Logger.i { "No school to update group $id" }
                    vppDatabase.groupDao.deleteById(listOf(id))
                    trySend(CacheState.Error(id.toString(), Response.Error.Other("no school for group $id")))
                    return@channelFlow
                }

                val response = httpClient.get("${api.url}/api/v2.2/group/$id") {
                    schoolApiAccess.authentication(this)
                }
                if (!response.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), response.toErrorResponse<Group>()))
                val data = ResponseDataWrapper.fromJson<GroupItemResponse>(response.bodyAsText())
                    ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))
                vppDatabase.groupDao.upsert(
                    DbGroup(
                        id = data.id,
                        name = data.name,
                        cachedAt = Clock.System.now()
                    ),
                    FKSchoolGroup(
                        schoolId = data.school.id,
                        groupId = data.id
                    )
                )

                return@channelFlow sendAll(getById(id, false))
            }
        }
    }

    override suspend fun updateFirebaseToken(group: Group, token: String): Response.Error? {
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url {
                    protocol = api.protocol
                    host = api.host
                    port = api.port
                    pathSegments = listOf("api", "v2.2", "group", group.id.toString(), "firebase")
                }
                group.school.getFirstValue()?.getSchoolApiAccess()?.authentication(this) ?: return Response.Error.Other("No school api access to update firebase token")
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
private data class SchoolItemGroupsResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("users") val users: Long,
)

@Serializable
private data class GroupUnauthenticatedResponse(
    @SerialName("school_id") val schoolId: Int
)

@Serializable
private data class GroupItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("school") val school: School
) {
    @Serializable
    data class School(
        @SerialName("id") val id: Int
    )
}