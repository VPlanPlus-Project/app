package plus.vplan.app.data.source.network

import plus.vplan.app.domain.data.Response
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertPathValidatorException

actual fun handleError(e: Exception): Response.Error? {
    return when(e) {
        is UnknownHostException, is SocketTimeoutException, is SocketException, is CertPathValidatorException -> Response.Error.OnlineError.ConnectionError
        else -> null
    }
}