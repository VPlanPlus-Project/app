package plus.vplan.app.feature.host.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.StartTask
import plus.vplan.app.domain.usecase.SetCurrentProfileUseCase
import plus.vplan.app.feature.grades.domain.usecase.LockGradesUseCase
import plus.vplan.app.feature.main.domain.usecase.SetupApplicationUseCase
import plus.vplan.app.feature.main.ui.MainScreenHost
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen
import plus.vplan.app.feature.schulverwalter.domain.usecase.UpdateSchulverwalterAccessUseCase
import plus.vplan.app.feature.vpp_id.ui.VppIdSetupScreen
import plus.vplan.app.utils.BrowserIntent

@Composable
fun NavigationHost(task: StartTask?) {
    val navigationHostController = rememberNavController()
    val viewModel = koinViewModel<NavigationHostViewModel>()
    val state = viewModel.state
    if (state.hasProfileAtAppStartup == null) return

    val localLayoutDirection = LocalLayoutDirection.current

    val top = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val left = WindowInsets.safeDrawing.asPaddingValues().calculateLeftPadding(localLayoutDirection)
    val right = WindowInsets.safeDrawing.asPaddingValues().calculateRightPadding(localLayoutDirection)
    with (LocalDensity.current) {

    }
    val bottom by animateDpAsState(WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding())
    val contentPadding = PaddingValues(left, top, right, bottom)

    val setCurrentProfileUseCase = koinInject<SetCurrentProfileUseCase>()

    val lockGradesUseCase = koinInject<LockGradesUseCase>()
    val setupApplicationUseCase = koinInject<SetupApplicationUseCase>()
    val updateSchulverwalterAccessUseCase = koinInject<UpdateSchulverwalterAccessUseCase>()

    LaunchedEffect(Unit) {
        lockGradesUseCase()
        setupApplicationUseCase()
    }

    LaunchedEffect(task) {
        if (task?.profileId != null) setCurrentProfileUseCase(task.profileId)
        when (task) {
            is StartTask.VppIdLogin -> navigationHostController.navigate(AppScreen.VppIdLogin(task.token))
            is StartTask.SchulverwalterReconnect -> navigationHostController.navigate(AppScreen.SchulverwalterReconnect(task.schulverwalterAccessToken, task.vppId))
            is StartTask.OpenUrl -> BrowserIntent.openUrl(task.url)
            else -> Unit
        }
    }

    NavHost(
        navController = navigationHostController,
        startDestination = if (state.hasProfileAtAppStartup == true) AppScreen.MainScreen else AppScreen.Onboarding(null)
    ) {
        composable<AppScreen.Onboarding> { route ->
            val args = route.toRoute<AppScreen.Onboarding>()
            OnboardingScreen(
                schoolId = args.schoolId,
            ) { navigationHostController.navigate(AppScreen.MainScreen) { popUpTo(0) } }
        }

        composable<AppScreen.MainScreen> {
            MainScreenHost(
                onNavigateToOnboarding = { navigationHostController.navigate(AppScreen.Onboarding(it?.id)) },
                contentPaddingDevice = contentPadding,
                navigationTask = task
            )
        }

        composable<AppScreen.VppIdLogin> { route ->
            val args = route.toRoute<AppScreen.VppIdLogin>()
            VppIdSetupScreen(
                token = args.token,
                onGoToHome = {
                    navigationHostController.navigate(AppScreen.MainScreen) { popUpTo(0) }
                }
            )
        }
        composable<AppScreen.SchulverwalterReconnect> { route ->
            val args = route.toRoute<AppScreen.SchulverwalterReconnect>()
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            LaunchedEffect(args) {
                updateSchulverwalterAccessUseCase(args.vppId, args.schulverwalterAccessToken)
                navigationHostController.navigateUp()
            }
        }
    }
}

@Serializable
sealed class AppScreen(val name: String) {
    @Serializable data object MainScreen : AppScreen("MainScreen")
    @Serializable data class Onboarding(val schoolId: Int?) : AppScreen("Onboarding")

    @Serializable data class VppIdLogin(val token: String) : AppScreen("VppIdLogin")
    @Serializable data class SchulverwalterReconnect(val schulverwalterAccessToken: String, val vppId: Int): AppScreen("SchulverwalterReconnect")
}