package plus.vplan.app.feature.onboarding.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.OnboardingSchoolSearch
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui.OnboardingIndiwareLoginScreen
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.ui.OnboardingIndiwareInitScreen
import plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.ui.OnboardingIndiwareDataDownloadScreen
import plus.vplan.app.feature.onboarding.stage.d_select_profile.ui.OnboardingSelectProfileScreen

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
fun OnboardingScreen() {
    Scaffold(
        modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = OnboardingScreen.OnboardingScreenHome,
            enterTransition = enterSlideTransition,
            exitTransition = exitSlideTransition,
            popEnterTransition = enterSlideTransitionRight,
            popExitTransition = exitSlideTransitionRight
        ) {
            composable<OnboardingScreen.OnboardingScreenHome> {
                OnboardingSchoolSearch(navController)
            }

            composable<OnboardingScreen.OnboardingScreenIndiwareLogin> {
                OnboardingIndiwareLoginScreen(navController)
            }

            composable<OnboardingScreen.OnboardingIndiwareInit> {
                OnboardingIndiwareInitScreen(navController)
            }

            composable<OnboardingScreen.OnboardingIndiwareDataDownload> {
                OnboardingIndiwareDataDownloadScreen(navController)
            }

            composable<OnboardingScreen.OnboardingChooseProfile> {
                OnboardingSelectProfileScreen(navController)
            }
        }
    }
}

@Serializable
sealed class OnboardingScreen(val name: String) {
    @Serializable data object OnboardingScreenHome : OnboardingScreen("OnboardingScreenHome")
    @Serializable data object OnboardingScreenIndiwareLogin : OnboardingScreen("OnboardingScreenIndiwareLogin")
    @Serializable data object OnboardingIndiwareInit : OnboardingScreen("OnboardingScreenInit")
    @Serializable data object OnboardingIndiwareDataDownload : OnboardingScreen("OnboardingScreenDataDownload")
    @Serializable data object OnboardingChooseProfile : OnboardingScreen("OnboardingScreenChooseProfileType")
}