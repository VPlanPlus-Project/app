package plus.vplan.app.feature.onboarding.stage.school_credentials.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Button
import plus.vplan.app.core.ui.components.ButtonSize
import plus.vplan.app.core.ui.components.ButtonState
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.components.PasswordField
import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.components.UsernameField
import plus.vplan.app.feature.onboarding.ui.components.OnboardingHeader


@Composable
fun Stundenplan24CredentialsScreen(
    sp24Id: Int?,
    isLoadingSchool: Boolean,
    onValidCredentialsProvided: (username: String, password: String) -> Unit,
) {
    val viewModel = koinViewModel<Stundenplan24CredentialsViewModel>()
    LaunchedEffect(sp24Id) {
        viewModel.init(sp24Id)
    }

    LaunchedEffect(isLoadingSchool) {
        viewModel.updateIsLoading(isLoadingSchool)
    }

    LaunchedEffect(Unit) {
        viewModel.handleEvent(Stundenplan24CredentialsEvent.OnScreenBecameActive)
    }

    val state by viewModel.state.collectAsState()

    if (state.sp24Id != null) Stundenplan24CredentialsContent(
        state = state,
        onEvent = viewModel::handleEvent
    )

    LaunchedEffect(state.isThisStageFinished) {
        if (!state.isThisStageFinished) return@LaunchedEffect

        onValidCredentialsProvided(state.username, state.password)
    }
}

@Composable
private fun Stundenplan24CredentialsContent(
    state: Stundenplan24CredentialsState,
    onEvent: (Stundenplan24CredentialsEvent) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val passwordFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        passwordFocusRequester.requestFocus()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(horizontal = 16.dp)
    ) {
        OnboardingHeader(
            title = "Zugangsdaten eingeben",
            subtitle = "Verwende die Zugangsdaten deiner Schule von Stundenplan24.de"
        )

        Column(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxWidth()
        ) {
            UsernameField(
                username = state.username,
                isUsernameValid = state.isUsernameValid,
                areCredentialsInvalid = state.sp24CredentialsState == Sp24CredentialsState.INVALID,
                onUsernameChanged = { onEvent(Stundenplan24CredentialsEvent.OnUsernameChanged(it)) },
                onFocusPassword = { passwordFocusRequester.requestFocus() },
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                password = state.password,
                passwordFocusRequester = passwordFocusRequester,
                areCredentialsInvalid = state.sp24CredentialsState == Sp24CredentialsState.INVALID,
                onPasswordChanged = { onEvent(Stundenplan24CredentialsEvent.OnPasswordChanged(it)) },
                onCheckCredentials = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEvent(Stundenplan24CredentialsEvent.OnCheckClicked)
                },
                shape = RoundedCornerShape(8.dp),
                passwordVisible = state.isPasswordVisible,
                onTogglePasswordVisible = { onEvent(Stundenplan24CredentialsEvent.SetPasswordVisible(it)) }
            )
        }

        Button(
            text = "Anmelden",
            state = if (state.isLoading) ButtonState.Disabled else state.sp24CredentialsState.toButtonState(),
            icon = CoreUiRes.drawable.arrow_right,
            onlyEventOnActive = true,
            modifier = Modifier.padding(bottom = 16.dp),
            size = ButtonSize.Big,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEvent(Stundenplan24CredentialsEvent.OnCheckClicked)
            }
        )
    }
}

@Preview
@Composable
private fun Stundenplan24CredentialsPreview() {
    AppTheme(dynamicColor = false) {
        Stundenplan24CredentialsContent(
            state = Stundenplan24CredentialsState(
                sp24Id = 10000000,
                username = "schueler",
                password = "password"
            ),
            onEvent = {}
        )
    }
}