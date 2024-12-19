package plus.vplan.app.feature.host.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen

@Composable
fun NavigationHost() {
    val navigationHostController = rememberNavController()
    val viewModel = koinViewModel<NavigationHostViewModel>()
    val state = viewModel.state
    if (state.hasProfile == null) return
    NavHost(
        navController = navigationHostController,
        startDestination = if (state.hasProfile == true) AppScreen.Home else AppScreen.Onboarding
    ) {
        composable<AppScreen.Onboarding> { OnboardingScreen() }
        composable<AppScreen.Home> { Text("Home") }
    }
}

@Serializable
sealed class AppScreen(val name: String) {
    @Serializable data object Home : AppScreen("Home")
    @Serializable data object Onboarding : AppScreen("Onboarding")
}