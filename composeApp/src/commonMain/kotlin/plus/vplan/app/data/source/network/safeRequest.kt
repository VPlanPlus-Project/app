package plus.vplan.app.data.source.network

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import plus.vplan.app.core.model.Response

expect fun handleError(e: Exception): Response.Error?

inline fun safeRequest(
    onError: (error: Response.Error) -> Unit,
    request: () -> Unit
) {
    try {
        request()
    } catch (e: Exception) {
        if (e !is CancellationException) Logger.e { "Error: ${e.stackTraceToString()}" }
        onError(
            handleError(e) ?: when (e) {
                is ClientRequestException, is ConnectionException, is HttpRequestTimeoutException -> Response.Error.OnlineError.ConnectionError
                is ServerResponseException -> Response.Error.Other(e.message)
                is CancellationException -> Response.Error.Cancelled
                else -> Response.Error.Other(e.stackTraceToString())
            }
        )
    }
}

class ConnectionException: Exception()