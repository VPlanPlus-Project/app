package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
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
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import plus.vplan.app.APP_ID
import plus.vplan.app.APP_REDIRECT_URI
import plus.vplan.app.APP_SECRET
import plus.vplan.app.api
import plus.vplan.app.auth
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbVppId
import plus.vplan.app.data.source.database.model.database.DbVppIdAccess
import plus.vplan.app.data.source.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.data.source.database.model.database.crossovers.DbVppIdGroupCrossover
import plus.vplan.app.data.source.network.isResponseFromBackend
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdDevice
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.utils.sendAll

private val logger = Logger.withTag("VppIdRepositoryImpl")

class VppIdRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : VppIdRepository {
    override suspend fun getAccessToken(code: String): Response<String> {
        safeRequest(onError = { return it }) {
            val response = httpClient.submitForm(
                url = "${auth.url}/oauth/token",
                formParameters = Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("client_id", APP_ID)
                    append("client_secret", APP_SECRET)
                    append("redirect_uri", APP_REDIRECT_URI)
                }
            )
            if (response.status != HttpStatusCode.OK) {
                logger.e { "Error getting access token: $response" }
                return response.toResponse()
            }
            val json = Json { ignoreUnknownKeys = true }
            val data = json.decodeFromString<TokenResponse>(response.bodyAsText())

            return Response.Success(data.accessToken)
        }
        return Response.Error.Cancelled
    }

    override suspend fun getUserByToken(token: String, upsert: Boolean): Response<VppId.Active> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "user", "me")
                }.buildString())
                bearerAuth(token)
            }
            if (response.status != HttpStatusCode.OK) {
                logger.e { "Error getting user by token: $response" }
                return response.toResponse()
            }
            val data = ResponseDataWrapper.fromJson<UserMeResponse>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            if (upsert) vppDatabase.vppIdDao.upsert(
                vppId = DbVppId(
                    id = data.id,
                    name = data.name,
                    cachedAt = Clock.System.now()
                ),
                vppIdAccess = DbVppIdAccess(
                    vppId = data.id,
                    accessToken = token
                ),
                vppIdSchulverwalter = data.schulverwalterAccessToken?.let {
                    DbVppIdSchulverwalter(
                        vppId = data.id,
                        schulverwalterAccessToken = it,
                        schulverwalterUserId = data.schulverwalterUserId!!,
                        isValid = null
                    )
                },
                groupCrossovers = listOf(
                    DbVppIdGroupCrossover(
                        vppId = data.id,
                        groupId = TODO()
                    )
                )
            )
            return Response.Success(getById(data.id, false).getFirstValueOld()!! as VppId.Active)
        }
        return Response.Error.Cancelled
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheStateOld<VppId>> {
        val vppIdFlow = vppDatabase.vppIdDao.getById(id).map { it?.toModel() }
        return channelFlow {
            if (!forceReload) {
                var hadData = false
                sendAll(vppIdFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheStateOld.Done(it) })
                if (hadData) return@channelFlow
            }
            send(CacheStateOld.Loading(id.toString()))

            safeRequest(onError = { trySend(CacheStateOld.Error(id, it)) }) {
                val existing = vppDatabase.vppIdDao.getById(id).first()?.toModel() as? VppId.Active
                val response = if (existing == null) {
                    val accessResponse = httpClient.get {
                        url(URLBuilder(api).apply {
                            appendPathSegments("api", "v2.2", "user", id.toString())
                        }.buildString())
                    }
                    if (accessResponse.status == HttpStatusCode.NotFound && accessResponse.isResponseFromBackend()) {
                        vppDatabase.vppIdDao.deleteById(listOf(id))
                        return@channelFlow send(CacheStateOld.NotExisting(id.toString()))
                    }
                    if (!accessResponse.status.isSuccess()) return@channelFlow send(CacheStateOld.Error(id.toString(), accessResponse.toErrorResponse<VppId>()))
                    val accessData = ResponseDataWrapper.fromJson<UserSchoolResponse>(accessResponse.bodyAsText())
                        ?: return@channelFlow send(CacheStateOld.Error(id.toString(), Response.Error.ParsingError(accessResponse.bodyAsText())))

                    val school = vppDatabase.schoolDao.getAll().first()
                        .map { it.toModel() }
                        .firstOrNull { it.getVppSchoolId() in accessData.schoolIds && it.getSchoolApiAccess() != null }
                        ?.getSchoolApiAccess() ?: return@channelFlow send(CacheStateOld.Error(id.toString(), Response.Error.Other("no school for vppId $id")))

                    httpClient.get {
                        url(URLBuilder(api).apply {
                            appendPathSegments("api", "v2.2", "user", id.toString())
                        }.buildString())

                        school.authentication(this)
                    }
                } else {
                    httpClient.get {
                        url(URLBuilder(api).apply {
                            appendPathSegments("api", "v2.2", "user", id.toString())
                        }.buildString())

                        existing.buildSchoolApiAccess().authentication(this)
                    }
                }

                if (!response.status.isSuccess()) return@channelFlow send(CacheStateOld.Error(id.toString(), response.toErrorResponse<VppId>()))
                val data = ResponseDataWrapper.fromJson<UserItemResponse>(response.bodyAsText())
                    ?: return@channelFlow send(CacheStateOld.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))

                vppDatabase.vppIdDao.upsert(
                    vppId = DbVppId(
                        id = data.id,
                        name = data.username,
                        cachedAt = Clock.System.now()
                    ),
                    groupCrossovers = data.groups.map {
                        DbVppIdGroupCrossover(
                            vppId = data.id,
                            groupId = TODO()
                        )
                    }
                )

                return@channelFlow sendAll(getById(data.id, false))
            }
        }
    }

    override fun getVppIds(): Flow<List<VppId>> {
        return vppDatabase.vppIdDao.getAll().map { flowData ->
            flowData.map { it.toModel() }
        }
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.vppIdDao.getAll().map { it.map { vppId -> vppId.vppId.id } }
    }

    override suspend fun getDevices(vppId: VppId.Active): Response<List<VppIdDevice>> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "user", "me", "session")
                }.buildString())

                bearerAuth(vppId.accessToken)
            }
            if (response.status != HttpStatusCode.OK) {
                logger.e { "Error getting devices: $response" }
                return response.toResponse()
            }

            val data = ResponseDataWrapper.fromJson<List<MeSession>>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            return Response.Success(data.map { it.toModel() })
        }
        return Response.Error.Cancelled
    }

    override suspend fun logoutDevice(vppId: VppId.Active, deviceId: Int): Response<Unit> {
        safeRequest(onError = { return it }) {
            val response = httpClient.delete {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "user", "me", "session", deviceId.toString())
                }.buildString())

                bearerAuth(vppId.accessToken)
            }
            if (response.status != HttpStatusCode.OK) {
                logger.e { "Error logging out device: $response" }
                return response.toResponse()
            }
            return Response.Success(Unit)
        }
        return Response.Error.Cancelled
    }

    override suspend fun logout(token: String): Response<Unit> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get("${auth.url}/oauth/logout") {
                bearerAuth(token)
            }
            if (response.status != HttpStatusCode.OK) {
                logger.e { "Error logging out: $response" }
                return response.toResponse()
            }
            return Response.Success(Unit)
        }
        return Response.Error.Cancelled
    }

    override suspend fun deleteAccessTokens(vppId: VppId.Active) {
        vppDatabase.vppIdDao.deleteAccessToken(vppId.id)
        vppDatabase.vppIdDao.deleteSchulverwalterAccessToken(vppId.id)
    }

    override suspend fun getSchulverwalterReauthUrl(vppId: VppId.Active): Response<String> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "app", "schulverwalter-reauth")
                }.buildString())
                vppId.buildSchoolApiAccess().authentication(this)
            }

            if (!response.status.isSuccess()) return response.toErrorResponse<String>()
            val url = ResponseDataWrapper.fromJson<String>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            return Response.Success(url)
        }
        return Response.Error.Cancelled
    }

    override suspend fun sendFeedback(access: SchoolApiAccess, content: String, email: String?): Response<Unit> {
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "app", "feedback")
                }.buildString())

                contentType(ContentType.Application.Json)
                setBody(FeedbackRequest(content, email))
                access.authentication(this)
            }
            if (response.status.isSuccess()) return Response.Success(Unit)
            return response.toErrorResponse<Unit>()
        }
        return Response.Error.Cancelled
    }

    override suspend fun updateFirebaseToken(vppId: VppId.Active, token: String): Response.Error? {
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "user", "firebase")
                }.buildString())

                bearerAuth(vppId.accessToken)
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
data class FirebaseTokenRequest(
    @SerialName("token") val token: String
)

@Serializable
private data class TokenResponse(
    @SerialName("access_token") val accessToken: String
)

@Serializable
private data class UserMeResponse(
    @SerialName("id") val id: Int,
    @SerialName("username") val name: String,
    @SerialName("group_id") val groupId: Int,
    @SerialName("schulverwalter_access_token") val schulverwalterAccessToken: String?,
    @SerialName("schulverwalter_user_id") val schulverwalterUserId: Int?,
)

@Serializable
data class UserItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val username: String,
    @SerialName("groups") val groups: List<Int>
)

@Serializable
private data class MeSession(
    @SerialName("session_id") val sessionId: Int,
    @SerialName("session_name") val sessionName: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("is_current") val isCurrent: Boolean,
    @SerialName("session_type") val sessionType: Char,
) {
    fun toModel(): VppIdDevice {
        return VppIdDevice(
            id = sessionId,
            name = sessionName,
            connectedAt = Instant.fromEpochSeconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault()),
            isThisDevice = isCurrent
        )
    }
}

@Serializable
data class UserSchoolResponse(
    @SerialName("school_ids") val schoolIds: List<Int>
)

@Serializable
data class FeedbackRequest(
    @SerialName("content") val content: String,
    @SerialName("email") val email: String?
)