package plus.vplan.app.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import co.touchlab.kermit.Logger
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.model.School
import plus.vplan.app.core.utils.ui.plus
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.finished.ui.FinishedScreen
import plus.vplan.app.feature.onboarding.stage.loading_data.ui.LoadingDataDialogContent
import plus.vplan.app.feature.onboarding.stage.permissions.ui.PermissionsScreen
import plus.vplan.app.feature.onboarding.stage.profile_selection.ui.ProfileSelectionScreen
import plus.vplan.app.feature.onboarding.stage.profile_selection.ui.SubjectInstanceSelectionScreen
import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.Stundenplan24CredentialsScreen
import plus.vplan.app.feature.onboarding.stage.school_select.ui.SchoolSearch
import plus.vplan.app.feature.onboarding.stage.teacher_notice.ui.TeacherNoticeScreen
import plus.vplan.app.feature.onboarding.stage.welcome.WelcomeScreen
import plus.vplan.app.feature.onboarding.stage.welcome.components.BlurredBackground
import plus.vplan.app.feature.onboarding.ui.components.CurrentStage
import plus.vplan.app.feature.onboarding.ui.components.ProgressIndicator

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = viewModel.backStack.lastOrNull() is Onboarding.Welcome,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BlurredBackground()
        }

        val contentPadding = WindowInsets.safeDrawing.asPaddingValues() + PaddingValues(top = 64.dp)

        Logger.d { "${WindowInsets.ime.asPaddingValues().calculateBottomPadding()} vs ${contentPadding.calculateBottomPadding()}" }

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
                            SchoolSearch(
                                onSchoolSelected = { viewModel.onSchoolSelected(it) },
                                contentPadding = contentPadding,
                            )
                        }
                    }
                    is Onboarding.SchoolCredentials -> {
                        NavEntry(key = key, metadata = transitionSpec) {
                            Stundenplan24CredentialsScreen(
                                sp24Id = onboardingState.selectedSchool?.sp24Id,
                                isLoadingSchool = onboardingState.isInitializingSchoolData,
                                contentPadding = contentPadding,
                                onValidCredentialsProvided = { username, password ->
                                    viewModel.onCredentialsProvided(username, password)
                                },
                            )
                            if (onboardingState.isInitializingSchoolData) LoadingDataDialogContent()
                        }
                    }
                    is Onboarding.ProfileSelection -> {
                        NavEntry(key = key, metadata = transitionSpec) {
                            ProfileSelectionScreen(
                                options = onboardingState.profileOptions,
                                contentPadding = contentPadding,
                                onProfileSelected = { viewModel.onProfileSelected(it) },
                            )
                        }
                    }
                    is Onboarding.TeacherNotice -> {
                        NavEntry(key = key, metadata = transitionSpec) {
                            TeacherNoticeScreen(
                                contentPadding = contentPadding,
                                onContinue = { viewModel.backStack.add(Onboarding.Permissions) }
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
                                    contentPadding = contentPadding,
                                    onDone = { viewModel.onSubjectInstanceSelectionDone() },
                                )
                            }
                        }
                    }
                    is Onboarding.Permissions -> {
                        NavEntry(key = key, metadata = transitionSpec) {
                            PermissionsScreen(
                                contentPadding = contentPadding,
                                onDone = { viewModel.onPermissionsDone() }
                            )
                        }
                    }
                    is Onboarding.Finished -> {
                        NavEntry(key = key, metadata = transitionSpec) {
                            FinishedScreen(
                                contentPadding = contentPadding,
                                onFinish = onFinish
                            )
                        }
                    }
                }
            }
        )

        AnimatedVisibility(
            visible = viewModel.backStack.lastOrNull().let { it != null && it !is Onboarding.Welcome },
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow,
                )
            ) + slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            ) { -it/3 } + fadeIn(),
            exit = fadeOut()
        ) {
            val currentStage = when (viewModel.backStack.lastOrNull()) {
                Onboarding.SchoolSelect -> CurrentStage.SchoolSearch
                Onboarding.SchoolCredentials -> CurrentStage.Credentials
                Onboarding.ProfileSelection -> CurrentStage.Profile
                Onboarding.TeacherNotice -> CurrentStage.ProfileConfiguration
                Onboarding.SubjectInstanceSelection -> CurrentStage.ProfileConfiguration
                Onboarding.Permissions -> CurrentStage.Notifications
                Onboarding.Finished -> CurrentStage.Done
                else -> null
            }
            if (currentStage != null) ProgressIndicator(
                currentStage = currentStage
            )
        }

    }
}
