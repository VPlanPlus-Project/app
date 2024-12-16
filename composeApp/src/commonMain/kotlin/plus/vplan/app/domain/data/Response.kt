package plus.vplan.app.domain.data

sealed class Response<out T> {
    data class Success<out T>(val data: T) : Response<T>()
    data object Loading: Response<Nothing>()
    sealed class Error : Response<Nothing>() {
        data class Other(val message: String = "Other error") : OnlineError()
        data object ParsingError : Error()
        data object Cancelled : Error()

        sealed class OnlineError: Error() {
            data object ConnectionError : OnlineError()
            data object Unauthorized : OnlineError()
            data object NotFound : OnlineError()
        }
    }
}