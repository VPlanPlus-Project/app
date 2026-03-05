package plus.vplan.app.feature.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.feature.main.ui.MainScreen
import plus.vplan.app.feature.settings.ui.components.SettingsRecord


@Composable
fun SettingsScreen(
    navHostController: NavHostController,
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state = viewModel.state

    SettingsContent(
        onBack = navHostController::navigateUp,
        onOpenSchoolSettings = remember { { navHostController.navigate(MainScreen.SchoolSettings()) } },
        onOpenSecuritySettings = remember { { navHostController.navigate(MainScreen.SecuritySettings) } },
        onOpenDeveloperSettings = remember { { navHostController.navigate(MainScreen.DeveloperSettings.Home) } },
        onOpenInfoAndFeedback = remember { { navHostController.navigate(MainScreen.InfoFeedbackSettings) } },
        state = state
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    onBack: () -> Unit,
    onOpenSchoolSettings: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
    onOpenDeveloperSettings: () -> Unit,
    onOpenInfoAndFeedback: () -> Unit,
    state: SettingsState
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(CoreUiRes.drawable.arrow_left),
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
                .padding(top = 4.dp)
        ) {
            SettingsRecord(
                title = "Meine Schulen",
                subtitle = "Zugänge für Schulen verwalten",
                icon = painterResource(CoreUiRes.drawable.school),
                onClick = onOpenSchoolSettings,
                showArrow = true,
            )
            HorizontalDivider(Modifier.padding(horizontal = 24.dp))
            SettingsRecord(
                title = "Anmeldung und Sicherheit",
                subtitle = "Noten und vpp.ID schützen",
                icon = painterResource(CoreUiRes.drawable.lock),
                onClick = onOpenSecuritySettings,
                showArrow = true,
            )
            HorizontalDivider(Modifier.padding(horizontal = 24.dp))
            if (state.isDeveloperSettingsEnabled) {
                SettingsRecord(
                    title = "Entwickleroptionen",
                    subtitle = "Flags, Diagnose und erweiterte Optionen",
                    icon = painterResource(CoreUiRes.drawable.bug_play),
                    onClick = onOpenDeveloperSettings,
                    showArrow = true,
                )
                HorizontalDivider(Modifier.padding(horizontal = 24.dp))
            }
            SettingsRecord(
                title = "Info & Feedback",
                subtitle = "Über VPlanPlus, Rückmeldung an die Entwickler",
                icon = painterResource(CoreUiRes.drawable.info),
                onClick = onOpenInfoAndFeedback,
                showArrow = true,
            )
            HorizontalDivider(Modifier.padding(horizontal = 24.dp))
        }
    }
}
