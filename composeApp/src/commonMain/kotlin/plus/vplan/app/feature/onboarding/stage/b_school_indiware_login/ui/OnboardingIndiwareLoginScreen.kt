package plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.Sp24CredentialsState
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui.component.Label
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui.component.PasswordField
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui.component.UsernameField
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_right

@Composable
fun OnboardingIndiwareLoginScreen(
    navController: NavHostController
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
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(bottom = 16.dp)
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
                areCredentialsInValid = state.sp24CredentialsState == Sp24CredentialsState.INVALID,
                onEvent = onEvent,
                onFocusPassword = { passwordFocusRequester.requestFocus() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                password = state.password,
                passwordFocusRequester = passwordFocusRequester,
                areCredentialsInvalid = state.sp24CredentialsState == Sp24CredentialsState.INVALID,
                onEvent = onEvent
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onEvent(OnboardingIndiwareLoginEvent.OnCheckClicked) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                AnimatedContent(
                    targetState = state.sp24CredentialsState
                ) { credentialsState ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (credentialsState == Sp24CredentialsState.LOADING) CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        ) else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("Anmelden")
                                Icon(
                                    painter = painterResource(Res.drawable.arrow_right),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
            LaunchedEffect(Unit) {
                passwordFocusRequester.requestFocus()
            }
        }
    }
}