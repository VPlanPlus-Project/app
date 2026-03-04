package plus.vplan.app.core.model

class NetworkException(
    val kind: NetworkErrorKind,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

enum class NetworkErrorKind {
    Unauthorized,
    NotFound,
    ConnectionError,
    ServerError,
    Cancelled,
    Other
}
