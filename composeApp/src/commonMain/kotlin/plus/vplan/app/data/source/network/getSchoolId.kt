package plus.vplan.app.data.source.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.asSuccess

suspend fun getAuthenticationOptionsForRestrictedEntity(
    httpClient: HttpClient,
    url: String
): Response<AuthenticationOptions> {
    safeRequest(onError = { return it }) {
        val response = httpClient.get(url)
        if (response.status != HttpStatusCode.Forbidden) return response.toErrorResponse()
        val schoolIdResponse = response.body<ResponseDataWrapper<AuthenticationOptionResponse>>()
        return AuthenticationOptions(
            schoolIds = schoolIdResponse.data.schoolIds,
            users = schoolIdResponse.data.userIds
        ).asSuccess()
    }
    return Response.Error.Cancelled
}

@Serializable
private data class AuthenticationOptionResponse(
    @SerialName("school_ids") val schoolIds: List<Int>? = null,
    @SerialName("user_ids") val userIds: List<Int>? = null
)

data class AuthenticationOptions(
    val schoolIds: List<Int>?,
    val users: List<Int>?
)