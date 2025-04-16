package plus.vplan.app.feature.dev.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.feature.profile.domain.usecase.UpdateAssessmentIndicesUseCase
import plus.vplan.app.feature.sync.domain.usecase.FullSyncUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase

@Composable
fun DevScreen(
    contentPadding: PaddingValues
) {
    val viewModel = koinViewModel<DevViewModel>()
    val state = viewModel.state
    val scope = rememberCoroutineScope()

    val updateTimetableUseCase = koinInject<UpdateTimetableUseCase>()
    val updateSubstitutionPlanUseCase = koinInject<UpdateSubstitutionPlanUseCase>()
    val fullSyncUseCase = koinInject<FullSyncUseCase>()
    val rebuildIndices = koinInject<UpdateAssessmentIndicesUseCase>()
    val updateHolidaysUseCase = koinInject<UpdateHolidaysUseCase>()
    val updateHomeworkUseCase = koinInject<UpdateHomeworkUseCase>()
    val courseRepository = koinInject<CourseRepository>()

    var fullSyncRunning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(state.profile?.name.toString())
        Button(
            onClick = { scope.launch {
                updateTimetableUseCase(state.profile!!.getSchoolItem() as School.IndiwareSchool, true)
            } }
        ) {
            Text("Stundenplan aktualisieren")
        }
        Button(
            onClick = { scope.launch {
                updateSubstitutionPlanUseCase(state.profile!!.getSchoolItem() as School.IndiwareSchool, listOf(LocalDate(2025, 4, 10)), true)?.let {
                    Logger.e { "Error on updating substitution plan: $it" }
                }
            } }
        ) {
            Text("VPlan aktualisieren")
        }
        Button(
            onClick = { scope.launch {
                updateHomeworkUseCase(true)
            } }
        ) {
            Text("Homework aktualisieren")
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    fullSyncRunning = true
                    fullSyncUseCase().invokeOnCompletion {
                        fullSyncRunning = false
                    }
                }
            ) {
                Text("Full sync")
            }
            if (fullSyncRunning) CircularProgressIndicator()
        }
        Button(
            onClick = { scope.launch {
                App.roomSource.getById(203, true).getFirstValue()
            } }
        ) {
            Text("Update R203")
        }
        Button(
            onClick = { scope.launch {
                updateHolidaysUseCase(state.profile!!.getSchool().getFirstValue() as School.IndiwareSchool)
            } }
        ) {
            Text("Update Holidays")
        }
        Button(
            onClick = { scope.launch {
                rebuildIndices(state.profile!!)
            } }
        ) {
            Text("Rebuild indices")
        }

        Button(onClick = { scope.launch {
            courseRepository.getAll().first()
                .take(1)
                .forEach { course ->
                    courseRepository.getById(course.id, true).getFirstValue()
                }
        } }) {
            Text("Course Update (force)")
        }
    }
}