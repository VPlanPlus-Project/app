package plus.vplan.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import plus.vplan.app.feature.host.ui.NavigationHost
import plus.vplan.app.ui.theme.AppTheme

const val SERVER_IP = "192.168.3.102"
val VPP_PROTOCOL = URLProtocol.HTTP
const val VPP_PORT = 8000
val VPP_ROOT_URL = "${VPP_PROTOCOL.name}://$SERVER_IP:$VPP_PORT"
const val VPP_SP24_URL = "http://$SERVER_IP:8080"
const val APP_ID = "4"
const val APP_SECRET = "secret"
const val APP_REDIRECT_URI = "vpp://app/auth/"
val VPP_ID_AUTH_URL = URLBuilder(
    protocol = URLProtocol.HTTP,
    host = SERVER_IP,
    port = 5174,
    pathSegments = listOf("authorize"),
    parameters = Parameters.build {
        append("client_id", APP_ID)
        append("client_secret", APP_SECRET)
        append("redirect_uri", APP_REDIRECT_URI)
        append("device_name", "mein telefon")
    }
).build().toString()

@Composable
@Preview
fun App(task: StartTask?) {
    AppTheme(dynamicColor = false) {
        KoinContext {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavigationHost(task)
                }
            }
        }
    }
}

sealed class StartTask {
    data class VppIdLogin(val token: String) : StartTask()
}