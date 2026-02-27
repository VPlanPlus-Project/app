package plus.vplan.app.network.besteschule

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess

class BesteSchuleApiImpl(
    private val httpClient: HttpClient,
) : BesteSchuleApi {
    override suspend fun checkValidity(token: String): Boolean {
        val response = httpClient.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "beste.schule"
                pathSegments = listOf("api", "user")
            }

            bearerAuth(token)
        }

        if (response.status.isSuccess()) return true
        if (response.status == HttpStatusCode.Unauthorized) return false

        else throw NetworkRequestUnsuccessfulException(response)
    }
}