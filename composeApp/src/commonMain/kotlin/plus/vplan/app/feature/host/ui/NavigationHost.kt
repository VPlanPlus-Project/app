package plus.vplan.app.feature.host.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.main.MainScreenHost
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen

@Composable
fun NavigationHost() {
    val navigationHostController = rememberNavController()
    val viewModel = koinViewModel<NavigationHostViewModel>()
    val state = viewModel.state
    if (state.hasProfile == null) return
    NavHost(
        navController = navigationHostController,
        startDestination = if (state.hasProfile == true) AppScreen.MainScreen else AppScreen.Onboarding
    ) {
        composable<AppScreen.Onboarding> { OnboardingScreen { navigationHostController.navigate(AppScreen.MainScreen) { popUpTo(0) } } }
        composable<AppScreen.MainScreen> { MainScreenHost() }
    }
}

@Serializable
sealed class AppScreen(val name: String) {
    @Serializable data object MainScreen : AppScreen("MainScreen")
    @Serializable data object Onboarding : AppScreen("Onboarding")
}