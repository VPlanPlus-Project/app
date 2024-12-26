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

const val VPP_ROOT_URL = "http://192.168.102.109:8000"
const val VPP_SP24_URL = "http://192.168.102.109:8080"
val VPP_ID_AUTH_URL = URLBuilder(
    protocol = URLProtocol.HTTP,
    host = "192.168.102.109",
    port = 5174,
    pathSegments = listOf("authorize"),
    parameters = Parameters.build {
        append("client_id", "4")
        append("client_secret", "secret")
        append("redirect_uri", "vpp://auth")
        append("device_name", "mein telefon")
    }
).build().toString()

@Composable
@Preview
fun App() {
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
                    NavigationHost()
                }
            }
        }
    }
}