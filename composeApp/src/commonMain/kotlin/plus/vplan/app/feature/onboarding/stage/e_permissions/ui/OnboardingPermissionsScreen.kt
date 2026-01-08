package plus.vplan.app.feature.onboarding.stage.e_permissions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_right
import vplanplus.composeapp.generated.resources.bell_ring

@Composable
fun OnboardingPermissionsScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<OnboardingPermissionViewModel>()
    val permissionFactory = rememberPermissionsControllerFactory()
    val permissionController = remember(permissionFactory) { permissionFactory.createPermissionsController() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val isPermissionGranted = permissionController.isPermissionGranted(Permission.REMOTE_NOTIFICATION)
        if (isPermissionGranted) viewModel.onNotificationGranted()
    }
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.bell_ring),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Wir benötigen deine Zustimmung",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Bitte erlaube VPlanPlus, Benachrichtigungen zu senden.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        ) {
            Button(
                text = "Weiter",
                state = ButtonState.Enabled,
                icon = Res.drawable.arrow_right,
                size = ButtonSize.Big,
                onlyEventOnActive = true,
                onClick = {
                    scope.launch {
                        try {
                            permissionController.providePermission(Permission.REMOTE_NOTIFICATION)
                            viewModel.onNotificationGranted()
                        } catch (_: DeniedException) {
                            viewModel.onNotificationDenied()
                        } catch (_: DeniedAlwaysException) {
                            viewModel.onNotificationDenied()
                        } catch (_: RequestCanceledException) {}
                    }
                }
            )
        }

        LaunchedEffect(viewModel.state.canContinue) {
            if (viewModel.state.canContinue) navHostController.navigate(OnboardingScreen.OnboardingFinished)
        }
    }
}