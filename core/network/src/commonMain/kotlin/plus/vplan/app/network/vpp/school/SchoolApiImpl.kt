package plus.vplan.app.network.vpp.school

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.model.Alias
import plus.vplan.app.network.besteschule.NetworkRequestUnsuccessfulException
import plus.vplan.app.network.besteschule.ResponseDataWrapper

class SchoolApiImpl(
    private val httpClient: HttpClient,
): SchoolApi {
    override suspend fun getAll(): List<SchoolDto> {
        val response = httpClient.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "vplan.plus"
                pathSegments = listOf("api", "app", "school", "v1", "list")
            }
        }

        if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

        return response.body<ResponseDataWrapper<List<ApiSchoolResponse>>>().data
            .map { it.toDto() }
    }

    override suspend fun getByAlias(alias: Alias): SchoolDto? {
        val response = httpClient.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "vplan.plus"
                pathSegments = listOf("api", "app", "school", "v1", "by-id", alias.toString())
            }
        }

        if (response.status == HttpStatusCode.NotFound) return null
        if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

        return response.body<ResponseDataWrapper<ApiSchoolResponse>>().data.toDto()
    }
}

@Serializable
private data class ApiSchoolResponse(
    @SerialName("school_id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("aliases") val aliases: List<Alias>
) {
    @Serializable
    data class Alias(
        @SerialName("type") val type: String,
        @SerialName("value") val value: String,
        @SerialName("version") val version: Int,
    ) {
        fun toDto() = SchoolDto.Alias(
            type = type,
            value = value,
            version = version,
        )
    }

    fun toDto() = SchoolDto(
        id = id,
        name = name,
        aliases = aliases.map { it.toDto() },
    )
}