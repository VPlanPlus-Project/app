package plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingIndiwareDataDownloadScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<OnboardingIndiwareDataDownloadViewModel>()
    OnboardingIndiwareDataDownloadContent()
}

@Composable
private fun OnboardingIndiwareDataDownloadContent() {
    Column {
        Text("Downloading Data to this device...")
    }
}