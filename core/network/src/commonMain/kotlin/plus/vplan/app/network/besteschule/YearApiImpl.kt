package plus.vplan.app.network.besteschule

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.database.dao.VppIdDao
import plus.vplan.app.network.NetworkRequestUnsuccessfulException

class YearApiImpl(
    private val httpClient: HttpClient,
    private val vppIdDao: VppIdDao
): YearApi {
    override suspend fun getById(id: Int): YearDto? {
        val accesses = vppIdDao.getSchulverwalterAccess().first()

        for (access in accesses) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "years")
                }
                bearerAuth(access.schulverwalterAccessToken)
            }

            if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
                continue
            }

            if (response.status == HttpStatusCode.NotFound) return null

            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

            val years = response.body<ResponseDataWrapper<List<YearApiResponse>>>().data
            val yearWithId = years.firstOrNull { it.id == id }
            if (yearWithId != null) return yearWithId.toDto()
        }

        throw Exception("No valid access token found")
    }

    override suspend fun getAll(): List<YearDto> {
        val accesses = vppIdDao.getSchulverwalterAccess().first()
        val items = mutableListOf<YearDto>()

        for (access in accesses) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "years")
                }
                bearerAuth(access.schulverwalterAccessToken)
            }

            if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
                continue
            }

            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

            items.addAll(response.body< ResponseDataWrapper<List<YearApiResponse>>>().data.map { it.toDto() })
        }

        return items.distinctBy { it.id }
    }

    override suspend fun setYear(userId: Int, yearId: Int?): Boolean {
        val access = vppIdDao.getSchulverwalterAccess().first().find { it.schulverwalterUserId == userId }
            ?: return false

        val response = httpClient.post {
            url {
                protocol = URLProtocol.HTTPS
                host = "beste.schule"
                port = 443
                pathSegments = listOf("api", "years", "current")
            }
            bearerAuth(access.schulverwalterAccessToken)

            contentType(ContentType.Application.Json)
            setBody(SetYearRequest(yearId))
        }

        return response.status.isSuccess()
    }
}

@Serializable
private data class YearApiResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
) {
    fun toDto() = YearDto(
        id = this.id,
        name = this.name,
        from = this.from,
        to = this.to,
    )
}

@Serializable
private data class SetYearRequest(
    @SerialName("id") val yearId: Int?
)