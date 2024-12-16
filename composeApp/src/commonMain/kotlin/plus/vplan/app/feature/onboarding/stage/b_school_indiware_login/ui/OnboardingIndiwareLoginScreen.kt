package plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingIndiwareLoginScreen() {

    val viewModel = koinViewModel<OnboardingIndiwareLoginViewModel>()
    val state = viewModel.state ?: return

    OnboardingIndiwareLoginContent(
        state = state,
        onEvent = viewModel::handleEvent
    )
}

@Composable
private fun OnboardingIndiwareLoginContent(
    state: OnboardingIndiwareLoginState,
    onEvent: (OnboardingIndiwareLoginEvent) -> Unit
) {
    Column {
        TextField(
            value = state.username,
            onValueChange = { onEvent(OnboardingIndiwareLoginEvent.OnUsernameChanged(it)) },
            label = { Text("Username") },
        )
        TextField(
            value = state.password,
            onValueChange = { onEvent(OnboardingIndiwareLoginEvent.OnPasswordChanged(it)) },
            label = { Text("Password") },
        )
        Button(
            onClick = { onEvent(OnboardingIndiwareLoginEvent.OnCheckClicked) },
        ) {
            Text("Next")
        }
        Text(state.sp24CredentialsState.toString())
    }
}