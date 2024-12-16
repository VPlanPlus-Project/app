package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.IndiwareRepository

class IndiwareRepositoryImpl(
    private val httpClient: HttpClient
) : IndiwareRepository {
    override suspend fun checkCredentials(
        sp24Id: Int,
        username: String,
        password: String
    ): Response<Boolean> {
        return saveRequest {
            val response = httpClient.get {
                url("https://stundenplan24.de/$sp24Id/mobil/")
                basicAuth(username, password)
            }
            if (response.status == HttpStatusCode.OK) return@saveRequest Response.Success(true)
            if (response.status == HttpStatusCode.Unauthorized) return@saveRequest Response.Success(false)
            return@saveRequest response.toResponse<Boolean>()
        }
    }
}