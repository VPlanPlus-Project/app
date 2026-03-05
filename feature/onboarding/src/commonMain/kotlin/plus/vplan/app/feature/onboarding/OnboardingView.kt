package plus.vplan.app.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.loading_data.ui.LoadingDataDialogContent
import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.Stundenplan24CredentialsScreen
import plus.vplan.app.feature.onboarding.stage.school_select.ui.SchoolSearch
import plus.vplan.app.feature.onboarding.stage.welcome.WelcomeScreen
import plus.vplan.app.feature.onboarding.stage.welcome.components.BlurredBackground

@Composable
fun OnboardingView() {
    val viewModel = koinViewModel<OnboardingViewModel>()
    val onboardingState by viewModel.state.collectAsState()


    LaunchedEffect(Unit) { viewModel.reset() }


    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = viewModel.backStack.lastOrNull() is Onboarding.Welcome,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BlurredBackground()
        }

        NavDisplay(
            backStack = viewModel.backStack,
            onBack = { viewModel.navigateBack() },
            entryProvider = { key ->
                return@NavDisplay when (key) {
                    is Onboarding.Welcome -> {
                        NavEntry(
                            key = key,
                            metadata = transitionSpec,
                        ) {
                            WelcomeScreen(
                                onNext = { viewModel.navigateToSchoolSelect() }
                            )
                        }
                    }
                    is Onboarding.SchoolSelect -> {
                        NavEntry(
                            key = key,
                            metadata = transitionSpec,
                        ) {
                            SchoolSearch(
                                onSchoolSelected = { viewModel.onSchoolSelected(it) }
                            )
                        }
                    }
                    is Onboarding.SchoolCredentials, is Onboarding.LoadingData -> {
                        NavEntry(
                            key = key,
                            metadata = transitionSpec,
                        ) {
                            Stundenplan24CredentialsScreen(
                                sp24Id = onboardingState.selectedSchool?.sp24Id,
                                onValidCredentialsProvided = { username, password ->
                                    viewModel.onCredentialsProvided(username, password)
                                },
                            )
                            if (it is Onboarding.LoadingData) LoadingDataDialogContent()
                        }
                    }
                    is Onboarding.ProfileSelection -> {
                        NavEntry(
                            key = key,
                            metadata = transitionSpec,
                        ) {
                            Text("Profile select")
                        }
                    }
                }
            }
        )
    }
}