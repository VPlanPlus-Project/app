package plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.Sp24CredentialsState
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left_right
import vplanplus.composeapp.generated.resources.arrow_right
import vplanplus.composeapp.generated.resources.eye
import vplanplus.composeapp.generated.resources.eye_off
import vplanplus.composeapp.generated.resources.rectangle_ellipsis
import vplanplus.composeapp.generated.resources.user

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
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            val passwordFocusRequester = remember { FocusRequester() }
            TextField(
                value = state.username,
                onValueChange = { onEvent(OnboardingIndiwareLoginEvent.OnUsernameChanged(it)) },
                label = { Text("Nutzername") },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                isError = !state.isUsernameValid || state.sp24CredentialsState == Sp24CredentialsState.INVALID,
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.user),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    var clickCount by remember { mutableStateOf(0) }
                    IconButton(
                        onClick = {
                            clickCount++
                            onEvent(OnboardingIndiwareLoginEvent.OnUsernameChanged(if (state.username == "lehrer") "schueler" else "lehrer"))
                        }
                    ) {
                        val rotation by animateFloatAsState(targetValue = clickCount * 180f)
                        Icon(
                            painter = painterResource(Res.drawable.arrow_left_right),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationY = rotation }
                        )
                    }
                }
            )
            AnimatedVisibility(
                visible = !state.isUsernameValid,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Text(
                    text = "Benutzername ungültig",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            var passwordVisible by remember { mutableStateOf(false) }
            TextField(
                value = state.password,
                onValueChange = { onEvent(OnboardingIndiwareLoginEvent.OnPasswordChanged(it)) },
                label = { Text("Passwort") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(
                    onDone = { onEvent(OnboardingIndiwareLoginEvent.OnCheckClicked) }
                ),
                isError = state.sp24CredentialsState == Sp24CredentialsState.INVALID,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.rectangle_ellipsis),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        AnimatedContent(
                            targetState = passwordVisible
                        ) { visible ->
                            Icon(
                                painter = painterResource(if (visible) Res.drawable.eye else Res.drawable.eye_off),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )

            AnimatedVisibility(
                visible = state.sp24CredentialsState == Sp24CredentialsState.INVALID,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Text(
                    text = "Zugangsdaten ungültig",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

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