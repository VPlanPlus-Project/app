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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.settings.ui.components.SettingsRecord
import plus.vplan.app.ui.components.TopToggle
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.rotate_cw
import vplanplus.composeapp.generated.resources.trash_2

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
            TopToggle(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "Entwickleroptionen",
                state = state.isDeveloperModeEnabled,
                onToggle = { onEvent(DeveloperSettingsEvent.ToggleDeveloperMode) },
            )

            SettingsRecord(
                title = "Vollständige Aktualisierung",
                subtitle = "Lädt alle Daten neu herunter",
                icon = painterResource(Res.drawable.rotate_cw),
                isLoading = state.isFullSyncRunning,
                enabled = !state.isFullSyncRunning,
                onClick = { onEvent(DeveloperSettingsEvent.StartFullSync) }
            )
            SettingsRecord(
                title = "Vertretungsplan aktualisieren",
                subtitle = "Erzwingt das Neuladen des Vertretungsplans",
                icon = painterResource(Res.drawable.rotate_cw),
                isLoading = state.isSubstitutionPlanUpdateRunning,
                enabled = !state.isSubstitutionPlanUpdateRunning,
                onClick = { onEvent(DeveloperSettingsEvent.UpdateSubstitutionPlan) }
            )
            SettingsRecord(
                title = "Stundenplan aktualisieren",
                subtitle = "Erzwingt das Neuladen des Stundenplans",
                icon = painterResource(Res.drawable.rotate_cw),
                isLoading = state.isTimetableUpdateRunning,
                enabled = !state.isTimetableUpdateRunning,
                onClick = { onEvent(DeveloperSettingsEvent.UpdateTimetable) }
            )
            SettingsRecord(
                title = "Wochen aktualisieren",
                subtitle = "Erzwingt das Neuladen der Wochen",
                icon = painterResource(Res.drawable.rotate_cw),
                isLoading = state.isWeekUpdateRunning,
                enabled = !state.isWeekUpdateRunning,
                onClick = { onEvent(DeveloperSettingsEvent.UpdateWeeks) }
            )
            SettingsRecord(
                title = "Hausaufgaben aktualisieren",
                subtitle = "Erzwingt das Neuladen der Hausaufgaben",
                icon = painterResource(Res.drawable.rotate_cw),
                isLoading = state.isHomeworkUpdateRunning,
                enabled = !state.isHomeworkUpdateRunning,
                onClick = { onEvent(DeveloperSettingsEvent.UpdateHomework) }
            )
            SettingsRecord(
                title = "Leistungserhebungen aktualisieren",
                subtitle = "Erzwingt das Neuladen der Leistungserhebungen",
                icon = painterResource(Res.drawable.rotate_cw),
                isLoading = state.isAssessmentsUpdateRunning,
                enabled = !state.isAssessmentsUpdateRunning,
                onClick = { onEvent(DeveloperSettingsEvent.UpdateAssessments) }
            )
            SettingsRecord(
                title = "Fachinstanzen und Kurse aktualisieren",
                subtitle = "Erzwingt das Neuladen der Fachinstanzen und Kurse",
                icon = painterResource(Res.drawable.rotate_cw),
                isLoading = state.isSubjectInstanceUpdateRunning,
                enabled = !state.isSubjectInstanceUpdateRunning,
                onClick = { onEvent(DeveloperSettingsEvent.UpdateSubjectInstances) }
            )
            SettingsRecord(
                title = "Stundenzeiten aktualisieren",
                subtitle = "Erzwingt das Neuladen der Stundenzeiten",
                icon = painterResource(Res.drawable.rotate_cw),
                isLoading = state.isLessonTimesUpdateRunning,
                enabled = !state.isLessonTimesUpdateRunning,
                onClick = { onEvent(DeveloperSettingsEvent.UpdateLessonTimes) }
            )

            HorizontalDivider(Modifier.padding(16.dp))

            SettingsRecord(
                title = "Stundencache leeren",
                subtitle = "Löscht alle lokal gespeicherten (Vertretungs)stunden",
                icon = painterResource(Res.drawable.trash_2),
                onClick = { onEvent(DeveloperSettingsEvent.ClearLessonCache) }
            )
            SettingsRecord(
                title = "Vertretungsplancache leeren",
                subtitle = "Löscht alle lokal gespeicherten Vertretungspläne",
                icon = painterResource(Res.drawable.trash_2),
                onClick = { onEvent(DeveloperSettingsEvent.ClearSubstitutionPlanCache) }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = !state.isAutoSyncDisabled,
                    onCheckedChange = remember { { onEvent(DeveloperSettingsEvent.ToggleAutoSyncDisabled) } }
                )
                Text("Auto-Sync aktivieren", modifier = Modifier.padding(start = 8.dp))
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