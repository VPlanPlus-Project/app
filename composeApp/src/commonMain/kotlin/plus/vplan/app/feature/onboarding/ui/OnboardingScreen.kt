package plus.vplan.app.feature.onboarding.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.OnboardingSchoolSearch
import plus.vplan.app.feature.onboarding.stage.a_welcome.ui.OnboardingWelcomeScreen
import plus.vplan.app.feature.onboarding.stage.a_welcome.ui.components.BlurredBackground
import plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.ui.OnboardingIndiwareLoginScreen
import plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.ui.OnboardingIndiwareDataDownloadScreen
import plus.vplan.app.feature.onboarding.stage.d_select_profile.ui.OnboardingSelectProfileScreen
import plus.vplan.app.feature.onboarding.stage.e_permissions.ui.OnboardingPermissionsScreen
import plus.vplan.app.feature.onboarding.stage.f_finished.ui.OnboardingFinishedScreen
import kotlin.uuid.Uuid

val enterSlideTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
    {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    }

val exitSlideTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
    {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    }

val enterSlideTransitionRight: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
    {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    }

val exitSlideTransitionRight: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
    {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    }

@Composable
fun OnboardingScreen(
    schoolId: Uuid?,
    skipIntroAnimation: Boolean,
    onFinish: () -> Unit,
) {
    val viewModel = koinViewModel<OnboardingHostViewModel>()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val navController = rememberNavController()

        LaunchedEffect(schoolId) {
            viewModel.init(schoolId)
            if (schoolId != null) navController.navigate(OnboardingScreen.OnboardingChooseProfile) {
                popUpTo(0)
            }
        }

        var showColorfulBackground by rememberSaveable { mutableStateOf(false) }
        AnimatedVisibility(
            visible = showColorfulBackground,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(Modifier.fillMaxSize()) { BlurredBackground() }
        }

        NavHost(
            navController = navController,
            startDestination = if (skipIntroAnimation) OnboardingScreen.OnboardingScreenSchoolSearch else OnboardingScreen.OnboardingScreenStart,
            enterTransition = enterSlideTransition,
            exitTransition = exitSlideTransition,
            popEnterTransition = enterSlideTransitionRight,
            popExitTransition = exitSlideTransitionRight
        ) {
            composable<OnboardingScreen.OnboardingScreenStart> {
                OnboardingWelcomeScreen(
                    onNext = remember { {
                        showColorfulBackground = false
                        navController.navigate(OnboardingScreen.OnboardingScreenSchoolSearch)
                    } }
                )
                DisposableEffect(Unit) {
                    showColorfulBackground = true
                    onDispose { showColorfulBackground = false }
                }
            }

            composable<OnboardingScreen.OnboardingScreenSchoolSearch> {
                OnboardingSchoolSearch(
                    navController = navController
                )
            }

            composable<OnboardingScreen.OnboardingScreenSp24Login> {
                OnboardingIndiwareLoginScreen(navController)
            }

            composable<OnboardingScreen.OnboardingIndiwareDataDownload> {
                OnboardingIndiwareDataDownloadScreen(navController)
            }

            composable<OnboardingScreen.OnboardingChooseProfile> {
                OnboardingSelectProfileScreen(navController)
            }

            composable<OnboardingScreen.OnboardingPermission> {
                OnboardingPermissionsScreen(navController)
            }

            composable<OnboardingScreen.OnboardingFinished> {
                OnboardingFinishedScreen(onFinish)
            }
        }
    }
}

@Serializable
sealed class OnboardingScreen(val name: String) {
    @Serializable data object OnboardingScreenStart : OnboardingScreen("OnboardingScreenStart")
    @Serializable data object OnboardingScreenSchoolSearch : OnboardingScreen("OnboardingScreenSchoolSearch")
    @Serializable data object OnboardingScreenSp24Login : OnboardingScreen("OnboardingScreenIndiwareLogin")
    @Serializable data object OnboardingIndiwareDataDownload : OnboardingScreen("OnboardingScreenDataDownload")
    @Serializable data object OnboardingChooseProfile : OnboardingScreen("OnboardingScreenChooseProfileType")
    @Serializable data object OnboardingPermission : OnboardingScreen("OnboardingScreenPermission")
    @Serializable data object OnboardingFinished : OnboardingScreen("OnboardingScreenFinished")
}