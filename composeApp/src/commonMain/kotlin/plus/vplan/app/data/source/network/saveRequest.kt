package plus.vplan.app.data.source.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import plus.vplan.app.domain.data.Response

suspend fun <T> saveRequest(
    request: suspend () -> Response<T>
): Response<T> {
    return try {
        request()
    } catch (e: Exception) {
        when (e) {
            is ClientRequestException -> Response.Error.OnlineError.ConnectionError
            is ServerResponseException -> Response.Error.Other(e.message)
            is ConnectionException -> Response.Error.OnlineError.ConnectionError
            is CancellationException -> Response.Error.Cancelled
            else -> Response.Error.Other(e.stackTraceToString())
        }
    }
}

class ConnectionException: Exception()