package plus.vplan.app.core.data.stundenplan24

import io.ktor.client.HttpClient
import plus.vplan.app.core.model.NetworkErrorKind
import plus.vplan.app.core.model.NetworkException
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client
import plus.vplan.lib.sp24.source.TestConnectionResult

class Stundenplan24RepositoryImpl(
    private val httpClient: HttpClient
) : Stundenplan24Repository {

    private val clients = mutableMapOf<Authentication, Stundenplan24Client>()

    override suspend fun checkCredentials(authentication: Authentication): Boolean {
        val client = clients.getOrPut(authentication) { Stundenplan24Client(authentication, httpClient) }
        return when (val result = client.testConnection()) {
            is TestConnectionResult.Success -> true
            is TestConnectionResult.Unauthorized -> false
            is TestConnectionResult.NotFound -> throw NetworkException(NetworkErrorKind.NotFound, "School not found on stundenplan24.de")
            else -> throw NetworkException(NetworkErrorKind.Other, result.toString())
        }
    }

    override suspend fun getSp24Client(authentication: Authentication, withCache: Boolean): Stundenplan24Client {
        if (withCache) return Stundenplan24Client(
            authentication = authentication,
            client = httpClient,
            enableInternalCache = true
        )
        return clients.getOrPut(authentication) {
            Stundenplan24Client(
                authentication = authentication,
                client = httpClient,
                enableInternalCache = false
            )
        }
    }
}
