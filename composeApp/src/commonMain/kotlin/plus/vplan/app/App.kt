package plus.vplan.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen

const val VPP_ROOT_URL = "http://192.168.3.102:8000"
const val VPP_SP24_URL = "http://192.168.3.102:8080"

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinContext {
            OnboardingScreen()
        }
    }
}