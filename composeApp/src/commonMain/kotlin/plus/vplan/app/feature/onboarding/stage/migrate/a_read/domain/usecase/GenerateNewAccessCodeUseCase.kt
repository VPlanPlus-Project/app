package plus.vplan.app.feature.onboarding.stage.migrate.a_read.domain.usecase

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import plus.vplan.app.api
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.feature.settings.page.info.domain.usecase.getSystemInfo

class GenerateNewAccessCodeUseCase(
    private val httpClient: HttpClient
) {
    suspend operator fun invoke(accessToken: String): String? {
        val response = httpClient.get {
            url {
                protocol = api.protocol
                host = api.host
                port = api.port
                pathSegments = listOf("api", "app", "old-token-migration")
                parameters {
                    append("device_name", getSystemInfo().let { "${it.manufacturer} ${it.deviceName} (${it.device})" })
                }
            }
            bearerAuth(accessToken)
        }
        if (!response.status.isSuccess()) return null
        return ResponseDataWrapper.fromJson(response.bodyAsText())
    }
}