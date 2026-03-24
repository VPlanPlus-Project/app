package plus.vplan.app.core.model.application.network

class ApiException(override val cause: Throwable?, coroutineContextName: String?): RuntimeException(buildString {
    appendLine("An API call failed.")
    appendLine()
    if (coroutineContextName != null) appendLine("Current Coroutine Context: $coroutineContextName")
})