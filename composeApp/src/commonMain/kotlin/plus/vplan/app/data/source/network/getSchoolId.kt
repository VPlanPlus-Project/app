package plus.vplan.app.data.source.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.data.asSuccess

suspend fun getSchoolIdForAuthentication(
    httpClient: HttpClient,
    url: String
): Response<Int> {
    safeRequest(onError = { return it }) {
        val response = httpClient.get(url)
        if (!response.status.isSuccess()) return response.toErrorResponse()
        val schoolIdResponse = response.body<ResponseDataWrapper<AnonymousSchoolIdResponse>>()
        return schoolIdResponse.data.schoolId.asSuccess()
    }
    return Response.Error.Cancelled
}

@Serializable
private data class AnonymousSchoolIdResponse(
    @SerialName("school_id") val schoolId: Int
)