package plus.vplan.app.feature.settings.page.security.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.settings.page.security.domain.usecase.BiometricDeviceState
import plus.vplan.app.feature.settings.page.security.ui.components.EnrollBiometricAuthenticationDialog
import plus.vplan.app.ui.platform.OpenBiometricSettings
import plus.vplan.app.ui.platform.RunBiometricAuthentication
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left

@Composable
fun SecuritySettingsScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<SecuritySettingsViewModel>()
    val state = viewModel.state

    val openBiometricSettings = koinInject<OpenBiometricSettings>()
    val runBiometricAuthentication = koinInject<RunBiometricAuthentication>()

    SecuritySettingsContent(
        state = state,
        onBack = remember { { navHostController.navigateUp() } },
        onEvent = viewModel::onEvent,
        onOpenBiometricSettings = openBiometricSettings::run,
        onRunBiometricAuthentication = remember { { onSuccess, onError, onCancel -> runBiometricAuthentication.run(
            title = "Sichere Anmeldung",
            subtitle = "Um die sichere Anmeldung zu deaktivieren, musst du Biometrie aktivieren.",
            negativeButtonText = "Abbrechen",
            onSuccess = onSuccess,
            onError = onError,
            onCancel = onCancel
        ) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecuritySettingsContent(
    state: SecuritySettingsState,
    onBack: () -> Unit,
    onEvent: (SecuritySettingsEvent) -> Unit,
    onOpenBiometricSettings: () -> Unit,
    onRunBiometricAuthentication: (onSuccess: () -> Unit, onError: () -> Unit, onCancel: () -> Unit) -> Unit
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    var showBiometricsEnrollDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anmeldung und Sicherheit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_left),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehaviour
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nestedScroll(scrollBehaviour.nestedScrollConnection)
        ) {
            Text("Noten schützen")
            Switch(
                checked = state.gradeProtectLevel != GradeProtectLevel.None,
                onCheckedChange = {
                    if (state.gradeProtectLevel == GradeProtectLevel.Biometric) {
                        onRunBiometricAuthentication(
                            { onEvent(SecuritySettingsEvent.ToggleGradeProtection) },
                            {},
                            {}
                        )
                    }
                    else onEvent(SecuritySettingsEvent.ToggleGradeProtection)
                }
            )
            if (state.biometricDeviceState != BiometricDeviceState.NotAvailable) {
                Text("Biometrische Anmeldung nutzen")
                Switch(
                    checked = state.gradeProtectLevel == GradeProtectLevel.Biometric,
                    onCheckedChange = {
                        if (state.gradeProtectLevel != GradeProtectLevel.Biometric && state.biometricDeviceState == BiometricDeviceState.NotEnrolled) showBiometricsEnrollDialog = true
                        else if (state.gradeProtectLevel == GradeProtectLevel.Biometric && state.biometricDeviceState == BiometricDeviceState.Ready) {
                            onRunBiometricAuthentication(
                                { onEvent(SecuritySettingsEvent.ToggleBiometricGradeProtection) },
                                {},
                                {}
                            )
                        }
                        else onEvent(SecuritySettingsEvent.ToggleBiometricGradeProtection)
                    }
                )
            }
            if (state.gradeProtectLevel == GradeProtectLevel.Regular && state.biometricDeviceState == BiometricDeviceState.NotEnrolled) {
                Text("Dein Gerät unterstützt Biometrische Anmeldung, du hast sie jedoch noch nicht eingerichtet. Gehe zu Einstellungen.")
            }
        }
    }

    if (showBiometricsEnrollDialog) EnrollBiometricAuthenticationDialog(
        onDismiss = { showBiometricsEnrollDialog = false },
        onOpenSettings = { onOpenBiometricSettings() }
    )
}