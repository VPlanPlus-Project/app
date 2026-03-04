package plus.vplan.app.core.model

import co.touchlab.kermit.Logger

sealed class Response<out T> {
    data class Success<out T>(val data: T) : Response<T>()
    data object Loading: Response<Nothing>()
    sealed class Error : Response<Nothing>() {
        data class Other(val message: String = "Other error") : OnlineError()
        data class ParsingError(val rawResponse: String) : Error() {
            init {
                Logger.e { "Parsing error: $rawResponse" }
            }
        }
        data object Cancelled : Error()

        sealed class OnlineError: Error() {
            data object ConnectionError : OnlineError()
            data object Unauthorized : OnlineError()
            data object NotFound : OnlineError()
            data object ServerError : OnlineError()
        }

        companion object {
            fun fromSp24KtError(response: plus.vplan.lib.sp24.source.Response.Error): Error {
                return when(response) {
                    is plus.vplan.lib.sp24.source.Response.Error.ParsingError -> ParsingError("")
                    is plus.vplan.lib.sp24.source.Response.Error.Other -> Other(response.message)
                    is plus.vplan.lib.sp24.source.Response.Error.Cancelled -> Cancelled
                    is plus.vplan.lib.sp24.source.Response.Error.OnlineError.ConnectionError -> OnlineError.ConnectionError
                    is plus.vplan.lib.sp24.source.Response.Error.OnlineError.Unauthorized -> OnlineError.Unauthorized
                    is plus.vplan.lib.sp24.source.Response.Error.OnlineError.NotFound -> OnlineError.NotFound
                }
            }
        }
    }
}

inline fun <reified T> T.asSuccess() = Response.Success(data = this)