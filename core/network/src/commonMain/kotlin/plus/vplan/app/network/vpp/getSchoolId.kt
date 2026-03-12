package plus.vplan.app.network.vpp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.network.NetworkRequestUnsuccessfulException
import plus.vplan.app.network.besteschule.ResponseDataWrapper

suspend fun getAuthenticationOptionsForRestrictedEntity(
    httpClient: HttpClient,
    url: String
): AuthenticationOptions? {
    val response = httpClient.get(url)
    if (response.status == HttpStatusCode.NotFound) return null
    if (response.status != HttpStatusCode.Forbidden) throw NetworkRequestUnsuccessfulException(
        response
    )
    val schoolIdResponse = response.body<ResponseDataWrapper<AuthenticationOptionResponse>>()
    return AuthenticationOptions(
        schoolIds = schoolIdResponse.data.schoolIds,
        userIds = schoolIdResponse.data.userIds
    )
}

@Serializable
private data class AuthenticationOptionResponse(
    @SerialName("school_ids") val schoolIds: List<Int>? = null,
    @SerialName("user_ids") val userIds: List<Int>? = null
)

data class AuthenticationOptions(
    val schoolIds: List<Int>?,
    val userIds: List<Int>?
)