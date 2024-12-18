package plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen

@Composable
fun OnboardingIndiwareDataDownloadScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<OnboardingIndiwareDataDownloadViewModel>()
    val state = viewModel.state
    OnboardingIndiwareDataDownloadContent(
        state = state
    )

    LaunchedEffect(state.success) {
        if (state.success == true) {
            navHostController.navigate(OnboardingScreen.OnboardingChooseProfile)
        }
    }
}

@Composable
private fun OnboardingIndiwareDataDownloadContent(
    state: OnboardingIndiwareDataDownloadUiState
) {
    Column {
        Text("Downloading Data to this device..., State: ${state.success}")
    }
}