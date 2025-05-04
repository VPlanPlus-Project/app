package plus.vplan.app.feature.onboarding.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.OnboardingSchoolSearch
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui.OnboardingIndiwareLoginScreen
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.ui.OnboardingIndiwareInitScreen
import plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.ui.OnboardingIndiwareDataDownloadScreen
import plus.vplan.app.feature.onboarding.stage.d_select_profile.ui.OnboardingSelectProfileScreen
import plus.vplan.app.feature.onboarding.stage.e_permissions.ui.OnboardingPermissionsScreen
import plus.vplan.app.feature.onboarding.stage.f_finished.ui.OnboardingFinishedScreen
import plus.vplan.app.feature.onboarding.stage.migrate.a_read.ui.ImportScreen

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
    schoolId: Int?,
    onFinish: () -> Unit,
) {
    val viewModel = koinViewModel<OnboardingHostViewModel>()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val navController = rememberNavController()

        LaunchedEffect(schoolId) {
            viewModel.init(schoolId)
            if (schoolId != null) navController.navigate(OnboardingScreen.OnboardingChooseProfile) {
                popUpTo(0)
            }
        }
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

            composable<OnboardingScreen.OnboardingPermission> {
                OnboardingPermissionsScreen(navController)
            }

            composable<OnboardingScreen.OnboardingFinished> {
                OnboardingFinishedScreen(onFinish)
            }

            composable<OnboardingScreen.OnboardingImportStart> {
                ImportScreen(navController)
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
    @Serializable data object OnboardingPermission : OnboardingScreen("OnboardingScreenPermission")
    @Serializable data object OnboardingFinished : OnboardingScreen("OnboardingScreenFinished")

   @Serializable data object OnboardingImportStart : OnboardingScreen("OnboardingImportStart")
}