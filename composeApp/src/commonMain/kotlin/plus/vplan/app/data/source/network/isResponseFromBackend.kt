package plus.vplan.app.data.source.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request

fun HttpResponse.isResponseFromBackend(): Boolean {
    return this.headers["X-Backend-Family"] == "vpp.ID" && setOf("vplan.plus", "vplanplus.localhost.dev").any { this.request.url.host.endsWith(it) }
}

fun HttpResponse.isFromStundenplan24(): Boolean {
    return this.request.url.host == "stundenplan24.de"
}

fun HttpResponse.isFromBesteSchule(): Boolean {
    return this.request.url.host == "beste.schule"
}