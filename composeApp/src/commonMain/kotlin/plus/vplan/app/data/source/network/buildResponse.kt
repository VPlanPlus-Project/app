package plus.vplan.app.data.source.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import plus.vplan.app.domain.data.Response

suspend fun <T> HttpResponse.toResponse(
    onSuccess: (httpResponse: HttpResponse) -> T = { TODO("OnSuccess is not implemented") }
): Response<T> {
    return when (status) {
        HttpStatusCode.OK -> Response.Success(onSuccess(this))
        HttpStatusCode.Unauthorized -> Response.Error.OnlineError.Unauthorized
        HttpStatusCode.NotFound -> Response.Error.OnlineError.NotFound
        HttpStatusCode.InternalServerError -> Response.Error.OnlineError.ServerError
        else -> throw UnsupportedOperationException(buildString {
            appendLine("The status code ${this@toResponse.status.value} (${this@toResponse.status.description}) is not yet supported.")
            appendLine("Body:")
            appendLine(this@toResponse.bodyAsText())
        })
    }
}

suspend fun HttpResponse.toErrorResponse(): Response.Error {
    return when (status) {
        HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> Response.Error.OnlineError.Unauthorized
        HttpStatusCode.NotFound -> {
            if (isResponseFromBackend() || isFromStundenplan24() || isFromBesteSchule()) Response.Error.OnlineError.NotFound
            else Response.Error.Other("The requested resource was not found on the server.")
        }
        HttpStatusCode.BadGateway -> Response.Error.OnlineError.ConnectionError
        HttpStatusCode.InternalServerError -> Response.Error.OnlineError.ServerError
        else -> throw UnsupportedOperationException(buildString {
            appendLine("The status code ${this@toErrorResponse.status.value} (${this@toErrorResponse.status.description}) is not yet supported.")
            appendLine("Body:")
            appendLine(this@toErrorResponse.bodyAsText())
        })
    }
}