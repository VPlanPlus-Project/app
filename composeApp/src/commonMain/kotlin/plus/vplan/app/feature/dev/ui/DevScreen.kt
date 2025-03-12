package plus.vplan.app.feature.dev.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.School
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileLessonIndexUseCase
import plus.vplan.app.feature.sync.domain.usecase.FullSyncUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase

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
    val rebuildIndices = koinInject<UpdateProfileLessonIndexUseCase>()

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
                updateSubstitutionPlanUseCase(state.profile!!.getSchoolItem() as School.IndiwareSchool, LocalDate(2025, 3, 12), true)
            } }
        ) {
            Text("VPlan aktualisieren")
        }
        Button(
            onClick = { scope.launch {
                fullSyncUseCase()
            } }
        ) {
            Text("Full sync")
        }
        Button(
            onClick = { scope.launch {
                rebuildIndices(state.profile!!)
            } }
        ) {
            Text("Rebuild indices")
        }
    }
}