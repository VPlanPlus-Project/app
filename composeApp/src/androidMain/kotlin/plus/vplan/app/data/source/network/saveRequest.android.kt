package plus.vplan.app.data.source.network

import plus.vplan.app.domain.data.Response
import java.net.UnknownHostException

actual fun handleError(e: Exception): Response.Error? {
    return when(e) {
        is UnknownHostException -> Response.Error.OnlineError.ConnectionError
        else -> null
    }
}