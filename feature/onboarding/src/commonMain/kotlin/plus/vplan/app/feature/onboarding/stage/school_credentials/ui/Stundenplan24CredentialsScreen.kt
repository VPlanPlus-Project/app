package plus.vplan.app.feature.onboarding.stage.school_credentials.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Button
import plus.vplan.app.core.ui.components.ButtonSize
import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.components.Label
import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.components.PasswordField
import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.components.UsernameField


@Composable
fun Stundenplan24CredentialsScreen(
    sp24Id: Int?,
    onValidCredentialsProvided: (username: String, password: String) -> Unit,
) {
    val viewModel = koinViewModel<Stundenplan24CredentialsViewModel>()
    LaunchedEffect(sp24Id) {
        viewModel.init(sp24Id)
    }

    LaunchedEffect(Unit) {
        viewModel.handleEvent(Stundenplan24CredentialsEvent.OnScreenBecameActive)
    }

    val state by viewModel.state.collectAsState()

    if (state.sp24Id != null) OnboardingIndiwareLoginContent(
        state = state,
        onEvent = viewModel::handleEvent
    )

    LaunchedEffect(state.isThisStageFinished) {
        if (!state.isThisStageFinished) return@LaunchedEffect

        onValidCredentialsProvided(state.username, state.password)
    }
}

@Composable
private fun OnboardingIndiwareLoginContent(
    state: Stundenplan24CredentialsState,
    onEvent: (Stundenplan24CredentialsEvent) -> Unit
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
                onUsernameChanged = { onEvent(Stundenplan24CredentialsEvent.OnUsernameChanged(it)) },
                onFocusPassword = { passwordFocusRequester.requestFocus() },
                hideBottomLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                password = state.password,
                passwordFocusRequester = passwordFocusRequester,
                areCredentialsInvalid = state.sp24CredentialsState == Sp24CredentialsState.INVALID,
                onPasswordChanged = { onEvent(Stundenplan24CredentialsEvent.OnPasswordChanged(it)) },
                onCheckCredentials = { onEvent(Stundenplan24CredentialsEvent.OnCheckClicked) },
                hideBottomLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                text = "Anmelden",
                state = state.sp24CredentialsState.toButtonState(),
                icon = CoreUiRes.drawable.arrow_right,
                onlyEventOnActive = true,
                size = ButtonSize.Big,
                onClick = { onEvent(Stundenplan24CredentialsEvent.OnCheckClicked) }
            )

            Spacer(Modifier.height(16.dp))


            LaunchedEffect(Unit) {
                passwordFocusRequester.requestFocus()
            }
        }
    }
}