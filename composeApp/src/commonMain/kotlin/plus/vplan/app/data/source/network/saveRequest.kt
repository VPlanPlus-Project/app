package plus.vplan.app.data.source.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import plus.vplan.app.domain.data.Response

inline fun <T> saveRequest(
    request: () -> Unit,
): Response<T> {
     try {
        request()
    } catch (e: Exception) {
        return when (e) {
            is ClientRequestException, is ConnectionException, is HttpRequestTimeoutException -> Response.Error.OnlineError.ConnectionError
            is ServerResponseException -> Response.Error.Other(e.message)
            is CancellationException -> Response.Error.Cancelled
            else -> Response.Error.Other(e.stackTraceToString())
        }
    }
    return Response.Error.Other("Not implemented")
}

class ConnectionException: Exception()