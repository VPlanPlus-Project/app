package plus.vplan.app.feature.settings.page.developer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.sync.domain.usecase.FullSyncCause
import plus.vplan.app.feature.sync.domain.usecase.FullSyncUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase
import plus.vplan.app.utils.now

class DeveloperSettingsViewModel(
    private val fullSyncUseCase: FullSyncUseCase,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) : ViewModel() {

    var state by mutableStateOf(DeveloperSettingsState())

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collect { profile ->
                state = state.copy(
                    profile = profile,
                )
            }
        }
    }

    fun handleEvent(event: DeveloperSettingsEvent) {
        viewModelScope.launch {
            when (event) {
                DeveloperSettingsEvent.StartFullSync -> {
                    if (state.isFullSyncRunning) return@launch
                    state = state.copy(isFullSyncRunning = true)
                    launch { fullSyncUseCase(FullSyncCause.Manual).join() }.invokeOnCompletion {
                        state = state.copy(isFullSyncRunning = false)
                    }
                }
                DeveloperSettingsEvent.ClearLessonCache -> {
                    substitutionPlanRepository.deleteAllSubstitutionPlans()
                    timetableRepository.deleteAllTimetables()
                }
                DeveloperSettingsEvent.UpdateSubstitutionPlan -> {
                    if (state.isSubstitutionPlanUpdateRunning) return@launch
                    state = state.copy(isSubstitutionPlanUpdateRunning = true)
                    updateSubstitutionPlanUseCase(
                        state.profile!!.getSchool().getFirstValue()!! as School.IndiwareSchool, listOf(LocalDate.now(), LocalDate.now().plus(1, DateTimeUnit.DAY)),
                        allowNotification = true
                    )
                    state = state.copy(isSubstitutionPlanUpdateRunning = false)
                }
                DeveloperSettingsEvent.UpdateTimetable -> {
                    if (state.isTimetableUpdateRunning) return@launch
                    state = state.copy(isTimetableUpdateRunning = true)
                    updateTimetableUseCase(state.profile!!.getSchool().getFirstValue()!! as School.IndiwareSchool, true)
                    state = state.copy(isTimetableUpdateRunning = false)
                }
            }
        }
    }
}

data class DeveloperSettingsState(
    val isFullSyncRunning: Boolean = false,
    val isSubstitutionPlanUpdateRunning: Boolean = false,
    val isTimetableUpdateRunning: Boolean = false,
    val profile: Profile? = null
)

sealed class DeveloperSettingsEvent {
    object StartFullSync : DeveloperSettingsEvent()
    object ClearLessonCache : DeveloperSettingsEvent()
    object UpdateSubstitutionPlan : DeveloperSettingsEvent()
    object UpdateTimetable : DeveloperSettingsEvent()
}