package plus.vplan.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinContext {
            OnboardingScreen()
        }
    }
}