package plus.vplan.app.network.besteschule

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.database.dao.VppIdDao

class IntervalApiImpl(
    private val httpClient: HttpClient,
    private val vppIdDao: VppIdDao,
): IntervalApi {
    override suspend fun getById(id: Int): IntervalDto? {
        val accesses = vppIdDao.getSchulverwalterAccess().first()

        for (access in accesses) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "intervals", id.toString())
                }
                bearerAuth(access.schulverwalterAccessToken)
            }

            if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
                continue
            }

            if (response.status == HttpStatusCode.NotFound) return null
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

            return response.body<ResponseDataWrapper<IntervalApiResponse>>().data.toDto()
        }

        throw Exception("No valid access token found")
    }

    override suspend fun getAll(): List<IntervalDto> {
        val accesses = vppIdDao.getSchulverwalterAccess().first()
        val items = mutableListOf<IntervalDto>()

        for (access in accesses) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "intervals")
                }
                bearerAuth(access.schulverwalterAccessToken)
            }

            if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
                continue
            }

            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

            items.addAll(response.body<ResponseDataWrapper<List<IntervalApiResponse>>>().data.map { it.toDto() })
        }

        return items.distinctBy { it.id }
    }
}

@Serializable
data class IntervalApiResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    @SerialName("included_interval_id") val includedIntervalId: Int?,
    @SerialName("year_id") val yearId: Int,
) {
    fun toDto() = IntervalDto(
        id = this.id,
        name = this.name,
        type = this.type,
        from = this.from,
        to = this.to,
        includedIntervalId = this.includedIntervalId,
        yearId = this.yearId,
    )
}