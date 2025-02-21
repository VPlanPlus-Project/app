package plus.vplan.app.data.source.network

import io.ktor.client.statement.HttpResponse

fun HttpResponse.isResponseFromBackend(): Boolean {
    return this.headers["X-Backend-Family"] == "vpp.ID"
}