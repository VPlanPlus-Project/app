package plus.vplan.app.feature.onboarding.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.OnboardingSchoolSearch
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui.OnboardingIndiwareLoginScreen

@Composable
fun OnboardingScreen() {
    Scaffold(
        modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = OnboardingScreen.OnboardingScreenHome
        ) {
            composable<OnboardingScreen.OnboardingScreenHome> {
                OnboardingSchoolSearch(navController)
            }

            composable<OnboardingScreen.OnboardingScreenIndiwareLogin> {
                OnboardingIndiwareLoginScreen()
            }
        }
    }
}

@Serializable
sealed class OnboardingScreen(val name: String) {
    @Serializable data object OnboardingScreenHome : OnboardingScreen("OnboardingScreenHome")
    @Serializable data object OnboardingScreenIndiwareLogin : OnboardingScreen("OnboardingScreenIndiwareLogin")
}