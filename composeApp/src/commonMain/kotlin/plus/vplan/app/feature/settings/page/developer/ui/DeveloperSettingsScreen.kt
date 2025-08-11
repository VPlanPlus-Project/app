package plus.vplan.app.feature.settings.page.developer.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left

@Composable
fun DeveloperSettingsScreen(
    navHostController: NavHostController,
) {
    val viewModel = koinViewModel<DeveloperSettingsViewModel>()
    val state = viewModel.state

    DeveloperSettingsContent(
        onBack = navHostController::navigateUp,
        onEvent = viewModel::handleEvent,
        state = state
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeveloperSettingsContent(
    onBack: () -> Unit,
    onEvent: (DeveloperSettingsEvent) -> Unit,
    state: DeveloperSettingsState
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entwicklereinstellungen") },
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
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row {
                Button(
                    onClick = remember { { onEvent(DeveloperSettingsEvent.StartFullSync) } },
                    enabled = !state.isFullSyncRunning,
                ) {
                    Text("Full sync")
                }
                if (state.isFullSyncRunning) CircularProgressIndicator(Modifier.padding(start = 8.dp))
            }
            Button(
                onClick = remember { { onEvent(DeveloperSettingsEvent.ClearLessonCache) } }
            ) {
                Text("Clear lesson cache")
            }
            Row {
                Button(
                    onClick = remember { { onEvent(DeveloperSettingsEvent.UpdateSubstitutionPlan) } },
                    enabled = !state.isSubstitutionPlanUpdateRunning,
                ) {
                    Text("Update substitution plan")
                }
                if (state.isSubstitutionPlanUpdateRunning) CircularProgressIndicator(Modifier.padding(start = 8.dp))
            }
            Row {
                Button(
                    onClick = remember { { onEvent(DeveloperSettingsEvent.UpdateTimetable) } },
                    enabled = !state.isTimetableUpdateRunning,
                ) {
                    Text("Update timetable")
                }
                if (state.isTimetableUpdateRunning) CircularProgressIndicator(Modifier.padding(start = 8.dp))
            }
            Row {
                Button(
                    onClick = remember { { onEvent(DeveloperSettingsEvent.UpdateWeeks) } },
                    enabled = !state.isWeekUpdateRunning,
                ) {
                    Text("Update weeks")
                }
                if (state.isWeekUpdateRunning) CircularProgressIndicator(Modifier.padding(start = 8.dp))
            }
            Row {
                Button(
                    onClick = remember { { onEvent(DeveloperSettingsEvent.UpdateSubjectInstances) } },
                    enabled = !state.isSubjectInstanceUpdateRunning,
                ) {
                    Text("Update subject instances + courses")
                }
                if (state.isSubjectInstanceUpdateRunning) CircularProgressIndicator(Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = !state.isAutoSyncDisabled,
                    onCheckedChange = remember { { onEvent(DeveloperSettingsEvent.ToggleAutoSyncDisabled) } }
                )
                Text("Auto-Sync aktivieren", modifier = Modifier.padding(start = 8.dp))
            }
            Button(onClick = remember { { onEvent(DeveloperSettingsEvent.DeleteSubstitutionPlan) } }) {
                Text("Drop substitution plan")
            }
            Text("FCM Logs", modifier = Modifier.padding(top = 8.dp))
            if (state.fcmLogs.isEmpty()) {
                Text("Keine FCM Logs vorhanden")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    state.fcmLogs.forEach { log ->
                        Text(
                            text = "${log.timestamp} - ${log.tag}: ${log.message}",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}