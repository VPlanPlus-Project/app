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
import plus.vplan.app.feature.main.MainScreen
import plus.vplan.app.feature.settings.ui.components.SettingsRecord
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.info
import vplanplus.composeapp.generated.resources.lock
import vplanplus.composeapp.generated.resources.school

@Composable
fun SettingsScreen(
    navHostController: NavHostController,
) {
    SettingsContent(
        onBack = navHostController::navigateUp,
        onOpenSchoolSettings = remember { { navHostController.navigate(MainScreen.SchoolSettings()) } },
        onOpenSecuritySettings = remember { { navHostController.navigate(MainScreen.SecuritySettings) } },
        onOpenInfoAndFeedback = remember { { navHostController.navigate(MainScreen.InfoFeedbackSettings) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    onBack: () -> Unit,
    onOpenSchoolSettings: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
    onOpenInfoAndFeedback: () -> Unit
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
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
                .padding(top = 4.dp)
        ) {
            SettingsRecord(
                title = "Meine Schulen",
                subtitle = "Zugänge für Schulen verwalten",
                icon = painterResource(Res.drawable.school),
                onClick = onOpenSchoolSettings
            )
            HorizontalDivider(Modifier.padding(horizontal = 24.dp))
            SettingsRecord(
                title = "Anmeldung und Sicherheit",
                subtitle = "Noten und vpp.ID schützen",
                icon = painterResource(Res.drawable.lock),
                onClick = onOpenSecuritySettings
            )
            HorizontalDivider(Modifier.padding(horizontal = 24.dp))
            SettingsRecord(
                title = "Info & Feedback",
                subtitle = "Über VPlanPlus, Rückmeldung an die Entwickler",
                icon = painterResource(Res.drawable.info),
                onClick = onOpenInfoAndFeedback
            )
            HorizontalDivider(Modifier.padding(horizontal = 24.dp))
        }
    }
}
