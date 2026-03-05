package plus.vplan.app.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.model.School
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.finished.ui.FinishedScreen
import plus.vplan.app.feature.onboarding.stage.loading_data.ui.LoadingDataDialogContent
import plus.vplan.app.feature.onboarding.stage.permissions.ui.PermissionsScreen
import plus.vplan.app.feature.onboarding.stage.profile_selection.ui.ProfileSelectionScreen
import plus.vplan.app.feature.onboarding.stage.profile_selection.ui.SubjectInstanceSelectionScreen
import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.Stundenplan24CredentialsScreen
import plus.vplan.app.feature.onboarding.stage.school_select.ui.SchoolSearch
import plus.vplan.app.feature.onboarding.stage.welcome.WelcomeScreen
import plus.vplan.app.feature.onboarding.stage.welcome.components.BlurredBackground

@Composable
fun OnboardingView(
    school: School.AppSchool? = null,
    onFinish: () -> Unit = {},
) {
    val viewModel = koinViewModel<OnboardingViewModel>()
    val onboardingState by viewModel.state.collectAsState()

    LaunchedEffect(school) {
        if (school != null) viewModel.initWithSchool(school)
        else viewModel.reset()
    }

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
                        NavEntry(key = key, metadata = transitionSpec) {
                            WelcomeScreen(onNext = { viewModel.navigateToSchoolSelect() })
                        }
                    }
                    is Onboarding.SchoolSelect -> {
                        NavEntry(key = key, metadata = transitionSpec) {
                            SchoolSearch(onSchoolSelected = { viewModel.onSchoolSelected(it) })
                        }
                    }
                    is Onboarding.SchoolCredentials, is Onboarding.LoadingData -> {
                        NavEntry(key = key, metadata = transitionSpec) {
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
                        NavEntry(key = key, metadata = transitionSpec) {
                            ProfileSelectionScreen(
                                options = onboardingState.profileOptions,
                                onProfileSelected = { viewModel.onProfileSelected(it) },
                            )
                        }
                    }
                    is Onboarding.SubjectInstanceSelection -> {
                        NavEntry(key = key, metadata = transitionSpec) {
                            val studentProfile = onboardingState.selectedOnboardingProfile
                                as? OnboardingProfile.StudentProfile
                            if (studentProfile != null) {
                                SubjectInstanceSelectionScreen(
                                    studentProfile = studentProfile,
                                    onBack = { viewModel.navigateBack() },
                                    onDone = { viewModel.onSubjectInstanceSelectionDone() },
                                )
                            }
                        }
                    }
                    is Onboarding.Permissions -> {
                        NavEntry(key = key, metadata = transitionSpec) {
                            PermissionsScreen(onDone = { viewModel.onPermissionsDone() })
                        }
                    }
                    is Onboarding.Finished -> {
                        NavEntry(key = key, metadata = transitionSpec) {
                            FinishedScreen(onFinish = onFinish)
                        }
                    }
                }
            }
        )
    }
}
