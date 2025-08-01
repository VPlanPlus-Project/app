package plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.domain.repository.Sp24CredentialsState
import plus.vplan.app.feature.onboarding.domain.repository.toButtonState
import plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.ui.component.Label
import plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.ui.component.PasswordField
import plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.ui.component.UsernameField
import plus.vplan.app.ui.components.ButtonSize
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_right

@Composable
fun OnboardingIndiwareLoginScreen(
    navController: NavHostController,
) {

    val viewModel = koinViewModel<OnboardingIndiwareLoginViewModel>()

    LaunchedEffect(Unit) {
        viewModel.init(navController)
    }

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
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        Label()
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            val passwordFocusRequester = remember { FocusRequester() }
            UsernameField(
                username = state.username,
                isUsernameValid = state.isUsernameValid,
                areCredentialsInvalid = state.sp24CredentialsState == Sp24CredentialsState.INVALID,
                onUsernameChanged = { onEvent(OnboardingIndiwareLoginEvent.OnUsernameChanged(it)) },
                onFocusPassword = { passwordFocusRequester.requestFocus() },
                hideBottomLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                password = state.password,
                passwordFocusRequester = passwordFocusRequester,
                areCredentialsInvalid = state.sp24CredentialsState == Sp24CredentialsState.INVALID,
                onPasswordChanged = { onEvent(OnboardingIndiwareLoginEvent.OnPasswordChanged(it)) },
                onCheckCredentials = { onEvent(OnboardingIndiwareLoginEvent.OnCheckClicked) },
                hideBottomLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            plus.vplan.app.ui.components.Button(
                text = "Anmelden",
                state = state.sp24CredentialsState.toButtonState(),
                icon = Res.drawable.arrow_right,
                onlyEventOnActive = true,
                size = ButtonSize.Big,
                onClick = { onEvent(OnboardingIndiwareLoginEvent.OnCheckClicked) }
            )

            Spacer(Modifier.height(16.dp))


            LaunchedEffect(Unit) {
                passwordFocusRequester.requestFocus()
            }
        }
    }
}