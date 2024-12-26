package plus.vplan.app.feature.host.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.StartTask
import plus.vplan.app.feature.main.MainScreenHost
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen
import plus.vplan.app.feature.vpp_id.ui.VppIdSetupScreen

@Composable
fun NavigationHost(task: StartTask?) {
    val navigationHostController = rememberNavController()
    val viewModel = koinViewModel<NavigationHostViewModel>()
    val state = viewModel.state
    if (state.hasProfile == null) return

    LaunchedEffect(task) {
        when (task) {
            is StartTask.VppIdLogin -> navigationHostController.navigate(AppScreen.VppIdLogin(task.token))
            else -> Unit
        }
    }

    NavHost(
        navController = navigationHostController,
        startDestination = if (state.hasProfile == true) AppScreen.MainScreen else AppScreen.Onboarding(null)
    ) {
        composable<AppScreen.Onboarding> { route ->
            val args = route.toRoute<AppScreen.Onboarding>()
            OnboardingScreen(args.schoolId) { navigationHostController.navigate(AppScreen.MainScreen) { popUpTo(0) } }
        }

        composable<AppScreen.MainScreen> {
            MainScreenHost {
                navigationHostController.navigate(AppScreen.Onboarding(it?.id))
            }
        }

        composable<AppScreen.VppIdLogin> { route ->
            val args = route.toRoute<AppScreen.VppIdLogin>()
            Text(args.token)
        }

        composable<AppScreen.VppIdLogin> { route ->
            val args = route.toRoute<AppScreen.VppIdLogin>()
            VppIdSetupScreen(args.token)
        }
    }
}

@Serializable
sealed class AppScreen(val name: String) {
    @Serializable data object MainScreen : AppScreen("MainScreen")
    @Serializable data class Onboarding(val schoolId: Int?) : AppScreen("Onboarding")

    @Serializable data class VppIdLogin(val token: String) : AppScreen("VppIdLogin")
}