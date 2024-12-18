package plus.vplan.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen
import plus.vplan.app.ui.theme.AppTheme

const val VPP_ROOT_URL = "http://192.168.3.102:8000"
const val VPP_SP24_URL = "http://192.168.3.102:8080"

@Composable
@Preview
fun App() {
    AppTheme(dynamicColor = false) {
        KoinContext {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                OnboardingScreen()
            }
        }
    }
}