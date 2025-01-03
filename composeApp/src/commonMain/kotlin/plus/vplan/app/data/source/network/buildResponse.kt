package plus.vplan.app.data.source.network

import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import plus.vplan.app.domain.data.Response

fun <T> HttpResponse.toResponse(
    onSuccess: (httpResponse: HttpResponse) -> T = { TODO("OnSuccess is not implemented") }
): Response<T> {
    return when (status) {
        HttpStatusCode.OK -> Response.Success(onSuccess(this))
        HttpStatusCode.Unauthorized -> Response.Error.OnlineError.Unauthorized
        HttpStatusCode.NotFound -> Response.Error.OnlineError.NotFound
        else -> throw UnsupportedOperationException("The status code ${status.value} is not supported at the moment")
    }
}

fun <T> HttpResponse.toErrorResponse(): Response.Error {
    return when (status) {
        HttpStatusCode.Unauthorized -> Response.Error.OnlineError.Unauthorized
        HttpStatusCode.NotFound -> Response.Error.OnlineError.NotFound
        else -> throw UnsupportedOperationException("The status code ${status.value} is not supported at the moment")
    }
}