package plus.vplan.app.core.data.vpp_id

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.DbVppId
import plus.vplan.app.core.database.model.database.DbVppIdAccess
import plus.vplan.app.core.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.core.database.model.database.crossovers.DbVppIdGroupCrossover
import plus.vplan.app.core.model.CreationReason
import plus.vplan.app.core.model.NetworkErrorKind
import plus.vplan.app.core.model.NetworkException
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.network.besteschule.ResponseDataWrapper
import plus.vplan.app.network.vpp.GenericAuthenticationProvider
import plus.vplan.app.network.vpp.getAuthenticationOptionsForRestrictedEntity
import plus.vplan.app.network.vpp.model.IncludedModel
import kotlin.time.Clock
import kotlin.time.Instant

private val logger = Logger.withTag("VppIdRepositoryImpl")

private const val AUTH_URL = "https://auth.vplan.plus"
private const val APP_API_URL = "https://vplan.plus/api/app"
private const val API_URL = "https://vplan.plus/api"
private const val APP_ID = "4"
private const val APP_SECRET = "crawling-mom-yesterday-jazz-populace-napkin"
private const val APP_REDIRECT_URI = "vpp://app/auth/"

private val jsonParser = Json { ignoreUnknownKeys = true }

private inline fun <reified T> parseWrapped(body: String): T? {
    return try {
        jsonParser.decodeFromString<ResponseDataWrapper<T>>(body).data
    } catch (_: Exception) {
        null
    }
}

class VppIdRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase,
    private val genericAuthenticationProvider: GenericAuthenticationProvider
) : VppIdRepository {

    override suspend fun getAccessToken(code: String): String = safe {
        val response = httpClient.submitForm(
            url = "$AUTH_URL/oauth/token",
            formParameters = Parameters.build {
                append("grant_type", "authorization_code")
                append("code", code)
                append("client_id", APP_ID)
                append("client_secret", APP_SECRET)
                append("redirect_uri", APP_REDIRECT_URI)
            }
        )
        if (response.status != HttpStatusCode.OK) {
            logger.e { "Error getting access token: ${response.status}" }
            throw response.toNetworkException()
        }
        jsonParser.decodeFromString<TokenResponse>(response.bodyAsText()).accessToken
    }

    override suspend fun getUserByToken(token: String): VppUserInfo = safe {
        val response = httpClient.get {
            url(URLBuilder(APP_API_URL).apply {
                appendPathSegments("user", "v1", "me")
            }.buildString())
            bearerAuth(token)
        }
        if (response.status == HttpStatusCode.Unauthorized) throw NetworkException(NetworkErrorKind.Unauthorized)
        if (response.status != HttpStatusCode.OK) {
            logger.e { "Error getting user by token: ${response.status}" }
            throw response.toNetworkException()
        }
        val data = parseWrapped<UserMeResponse>(response.bodyAsText())
            ?: throw NetworkException(NetworkErrorKind.Other, "Parsing error")

        VppUserInfo(
            id = data.id,
            username = data.name,
            groupId = data.group.id,
            schoolId = data.school.id,
            schulverwalterId = data.schulverwalterUserId,
            schulverwalterAccessToken = data.schulverwalterAccessToken
        )
    }

    override fun getById(id: Int): Flow<VppId?> {
        return vppDatabase.vppIdDao.getById(id).map { it?.toModel() }
    }

    override fun getVppIds(): Flow<List<VppId>> {
        return vppDatabase.vppIdDao.getAll().map { it.map { item -> item.toModel() } }
    }

    override fun getAllLocalIds(): Flow<List<Int>> {
        return vppDatabase.vppIdDao.getAll().map { it.map { item -> item.vppId.id } }
    }

    override suspend fun getDevices(vppId: VppId.Active): List<VppIdDevice> = safe {
        val response = httpClient.get {
            url(URLBuilder(API_URL).apply {
                appendPathSegments("api", "v2.2", "user", "me", "session")
            }.buildString())
            bearerAuth(vppId.accessToken)
        }
        if (response.status != HttpStatusCode.OK) {
            logger.e { "Error getting devices: ${response.status}" }
            throw response.toNetworkException()
        }
        val data = parseWrapped<List<MeSession>>(response.bodyAsText())
            ?: throw NetworkException(NetworkErrorKind.Other, "Parsing error")
        data.map { it.toModel() }
    }

    override suspend fun logoutDevice(vppId: VppId.Active, deviceId: Int): Unit = safe {
        val response = httpClient.delete {
            url(URLBuilder(API_URL).apply {
                appendPathSegments("api", "v2.2", "user", "me", "session", deviceId.toString())
            }.buildString())
            bearerAuth(vppId.accessToken)
        }
        if (response.status != HttpStatusCode.OK) {
            logger.e { "Error logging out device: ${response.status}" }
            throw response.toNetworkException()
        }
    }

    override suspend fun logout(token: String): Unit = safe {
        val response = httpClient.get("$AUTH_URL/oauth/logout") {
            bearerAuth(token)
        }
        if (response.status != HttpStatusCode.OK) {
            logger.e { "Error logging out: ${response.status}" }
            throw response.toNetworkException()
        }
    }

    override suspend fun deleteAccessTokens(vppId: VppId.Active) {
        vppDatabase.vppIdDao.deleteAccessToken(vppId.id)
        vppDatabase.vppIdDao.deleteSchulverwalterAccessToken(vppId.id)
    }

    override suspend fun getSchulverwalterReauthUrl(vppId: VppId.Active): String = safe {
        val response = httpClient.get {
            url(URLBuilder(API_URL).apply {
                appendPathSegments("api", "v2.2", "app", "schulverwalter-reauth")
            }.buildString())
            vppId.buildVppSchoolAuthentication().authentication(this)
        }
        if (!response.status.isSuccess()) throw response.toNetworkException()
        parseWrapped<String>(response.bodyAsText())
            ?: throw NetworkException(NetworkErrorKind.Other, "Parsing error")
    }

    override suspend fun sendFeedback(access: VppSchoolAuthentication, content: String, email: String?): Unit = safe {
        val response = httpClient.post {
            url(URLBuilder(APP_API_URL).apply {
                appendPathSegments("app", "feedback", "v1")
            }.buildString())
            contentType(ContentType.Application.Json)
            setBody(FeedbackRequest(content, email))
            access.authentication(this)
        }
        if (!response.status.isSuccess()) throw response.toNetworkException()
    }

    override suspend fun updateFirebaseToken(vppId: VppId.Active, token: String): Unit = safe {
        val response = httpClient.post {
            url(URLBuilder(API_URL).apply {
                appendPathSegments("api", "v2.2", "user", "firebase")
            }.buildString())
            bearerAuth(vppId.accessToken)
            contentType(ContentType.Application.Json)
            setBody(FirebaseTokenRequest(token))
        }
        if (!response.status.isSuccess()) throw response.toNetworkException()
    }

    override suspend fun save(vppId: VppId) {
        when (vppId) {
            is VppId.Cached -> vppDatabase.vppIdDao.upsert(
                vppId = DbVppId(
                    id = vppId.id,
                    name = vppId.name,
                    cachedAt = Clock.System.now(),
                    creationReason = if (vppDatabase.vppIdDao.getById(vppId.id).first()?.vppId?.creationReason == CreationReason.Persisted)
                        CreationReason.Persisted else CreationReason.Cached
                ),
                groupCrossovers = vppId.groups.map { DbVppIdGroupCrossover(vppId.id, it) }
            )

            is VppId.Active -> vppDatabase.vppIdDao.upsert(
                vppId = DbVppId(
                    id = vppId.id,
                    name = vppId.name,
                    cachedAt = Clock.System.now(),
                    creationReason = CreationReason.Persisted
                ),
                vppIdAccess = DbVppIdAccess(vppId.id, vppId.accessToken),
                vppIdSchulverwalter = vppId.schulverwalterConnection?.let {
                    DbVppIdSchulverwalter(vppId.id, it.userId, it.accessToken, it.isValid ?: true)
                },
                groupCrossovers = vppId.groups.map { DbVppIdGroupCrossover(vppId.id, it) }
            )
        }
    }

    override suspend fun downloadAndSave(id: Int): VppId? = safe {
        val authenticationResponse = getAuthenticationOptionsForRestrictedEntity(
            httpClient = httpClient,
            url = URLBuilder(APP_API_URL).apply {
                appendPathSegments("user", "v1", id.toString())
            }.buildString()
        ) ?: return@safe null

        val authentication = genericAuthenticationProvider.getAuthentication(authenticationResponse)
            ?: throw NetworkException(NetworkErrorKind.Other, "No authentication found for vppId $id")

        val response = httpClient.get {
            url(URLBuilder(APP_API_URL).apply {
                appendPathSegments("user", "v1", id.toString())
            }.buildString())
            authentication.authentication(this)
        }

        if (response.status == HttpStatusCode.NotFound) {
            vppDatabase.vppIdDao.deleteById(listOf(id))
            return@safe null
        }

        if (!response.status.isSuccess()) {
            logger.e { "Error getting vppId by id: ${response.status}" }
            throw response.toNetworkException()
        }

        val data = response.body<ResponseDataWrapper<UserItemResponse>>().data
        val existing = vppDatabase.vppIdDao.getById(id).first()

        vppDatabase.vppIdDao.upsert(
            vppId = DbVppId(
                id = data.id,
                name = data.username,
                cachedAt = Clock.System.now(),
                creationReason = if (existing?.vppId?.creationReason == CreationReason.Persisted)
                    CreationReason.Persisted else CreationReason.Cached
            ),
            groupCrossovers = data.groups.map { DbVppIdGroupCrossover(data.id, it) }
        )

        vppDatabase.vppIdDao.getById(id).first()?.toModel()
    }

    override suspend fun checkTokenValidity(accessToken: String): Boolean = safe {
        val response = httpClient.get {
            url(URLBuilder(APP_API_URL).apply {
                appendPathSegments("user", "v1", "me")
            }.buildString())
            bearerAuth(accessToken)
        }
        when (response.status) {
            HttpStatusCode.OK -> true
            HttpStatusCode.Unauthorized -> false
            else -> throw response.toNetworkException()
        }
    }

    override suspend fun logSp24Credentials(authentication: VppSchoolAuthentication.Sp24) {
        val logLogger = Logger.withTag("VppIdRepositoryImpl.logSp24Credentials")
        try {
            val response = httpClient.post {
                url(URLBuilder(APP_API_URL).apply {
                    appendPathSegments("sp24", "v1", "log")
                }.buildString())
                contentType(ContentType.Application.Json)
                setBody(
                    Sp24LogRequest(
                        sp24Id = authentication.sp24SchoolId.toInt(),
                        username = authentication.username,
                        password = authentication.password
                    )
                )
                authentication.authentication(this)
            }
            if (response.status == HttpStatusCode.OK) logLogger.i { "Successfully logged sp24 credentials" }
            else logLogger.e { "Error logging sp24 credentials: ${response.status} - ${response.bodyAsText()}" }
        } catch (e: Exception) {
            logLogger.e { "Exception logging sp24 credentials: ${e.message}" }
        }
    }
}

// ---- Helpers ----

private suspend fun HttpResponse.toNetworkException(): NetworkException {
    return when (this.status) {
        HttpStatusCode.Unauthorized -> NetworkException(NetworkErrorKind.Unauthorized)
        HttpStatusCode.NotFound -> NetworkException(NetworkErrorKind.NotFound)
        HttpStatusCode.InternalServerError -> NetworkException(NetworkErrorKind.ServerError)
        else -> NetworkException(NetworkErrorKind.Other, "HTTP ${this.status.value}: ${this.bodyAsText()}")
    }
}

private suspend inline fun <T> safe(block: suspend () -> T): T {
    return try {
        block()
    } catch (e: NetworkException) {
        throw e
    } catch (e: CancellationException) {
        throw NetworkException(NetworkErrorKind.Cancelled, cause = e)
    } catch (e: ClientRequestException) {
        throw NetworkException(NetworkErrorKind.ConnectionError, cause = e)
    } catch (e: HttpRequestTimeoutException) {
        throw NetworkException(NetworkErrorKind.ConnectionError, cause = e)
    } catch (e: ServerResponseException) {
        throw NetworkException(NetworkErrorKind.ServerError, cause = e)
    } catch (e: Exception) {
        throw NetworkException(NetworkErrorKind.Other, message = e.message, cause = e)
    }
}

// ---- Network response models ----

@Serializable
private data class TokenResponse(
    @SerialName("access_token") val accessToken: String
)

@Serializable
private data class UserMeResponse(
    @SerialName("id") val id: Int,
    @SerialName("username") val name: String,
    @SerialName("group") val group: IncludedModel,
    @SerialName("school") val school: IncludedModel,
    @SerialName("schulverwalter_access_token") val schulverwalterAccessToken: String?,
    @SerialName("schulverwalter_user_id") val schulverwalterUserId: Int?,
)

@Serializable
private data class MeSession(
    @SerialName("session_id") val sessionId: Int,
    @SerialName("session_name") val sessionName: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("is_current") val isCurrent: Boolean,
) {
    fun toModel(): VppIdDevice = VppIdDevice(
        id = sessionId,
        name = sessionName,
        connectedAt = Instant.fromEpochSeconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault()),
        isThisDevice = isCurrent
    )
}

@Serializable
data class FeedbackRequest(
    @SerialName("content") val content: String,
    @SerialName("email") val email: String?
)

@Serializable
private data class FirebaseTokenRequest(
    @SerialName("token") val token: String
)

@Serializable
private data class Sp24LogRequest(
    @SerialName("sp24_id") val sp24Id: Int,
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

@Serializable
private data class UserItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val username: String,
    @SerialName("groups") val groups: List<Int>
)
