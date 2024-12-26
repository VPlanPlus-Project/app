package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import plus.vplan.app.APP_ID
import plus.vplan.app.APP_REDIRECT_URI
import plus.vplan.app.APP_SECRET
import plus.vplan.app.VPP_ROOT_URL
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbVppId
import plus.vplan.app.data.source.database.model.database.DbVppIdAccess
import plus.vplan.app.data.source.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.data.source.database.model.database.crossovers.DbVppIdGroupCrossover
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

private val logger = Logger.withTag("VppIdRepositoryImpl")

class VppIdRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : VppIdRepository {
    override suspend fun getAccessToken(code: String): Response<String> {
        return saveRequest {
            val response = httpClient.submitForm(
                url = "$VPP_ROOT_URL/api/v2.2/auth/token",
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
    }

    override suspend fun getUserByToken(token: String): Response<VppId.Active> {
        return saveRequest {
            val response = httpClient.get("$VPP_ROOT_URL/api/v2.2/user/me") {
                bearerAuth(token)
            }
            if (response.status != HttpStatusCode.OK) {
                logger.e { "Error getting user by token: $response" }
                return response.toResponse()
            }
            val data = ResponseDataWrapper.fromJson<UserResponse>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            vppDatabase.vppIdDao.upsert(
                vppId = DbVppId(
                    id = data.id,
                    name = data.name,
                    cachedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                ),
                vppIdAccess = DbVppIdAccess(
                    vppId = data.id,
                    accessToken = token
                ),
                vppIdSchulverwalter = data.schulverwalterAccessToken?.let {
                    DbVppIdSchulverwalter(
                        vppId = data.id,
                        schulverwalterAccessToken = it
                    )
                },
                groupCrossovers = listOf(
                    DbVppIdGroupCrossover(
                        vppId = data.id,
                        groupId = data.groupId
                    )
                )
            )
            return Response.Success(getVppIdById(data.id).first() as VppId.Active)
        }
    }

    override suspend fun getVppIdById(id: Int): Flow<VppId?> {
        return vppDatabase.vppIdDao.getById(id).map { it?.toModel() }
    }

    override suspend fun getVppIdByIdWithCaching(id: Int): Response<VppId> {
        TODO("Not yet implemented")
    }

    override fun getVppIds(): Flow<List<VppId>> {
        return vppDatabase.vppIdDao.getAll().map { flowData ->
            flowData.map { it.toModel() }
        }
    }
}

@Serializable
private data class TokenResponse(
    @SerialName("access_token") val accessToken: String
)

@Serializable
private data class UserResponse(
    @SerialName("id") val id: Int,
    @SerialName("username") val name: String,
    @SerialName("group_id") val groupId: Int,
    @SerialName("schulverwalter_access_token") val schulverwalterAccessToken: String?,
)