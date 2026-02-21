package plus.vplan.app.network.besteschule

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import kotlinx.coroutines.runBlocking

class NetworkRequestUnsuccessfulException(response: HttpResponse): RuntimeException(buildString {
    appendLine("A request failed.")
    appendLine("To: ${response.request.url}")
    appendLine("Status: ${response.status}")
    runBlocking {
        appendLine("Body: ${response.bodyAsText()}")
    }

    appendLine()
    appendLine("This status is not expected. Please file a bug.")
})