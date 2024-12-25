package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SchoolDay
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.domain.usecase.GetDayUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase

class HomeViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getDayUseCase: GetDayUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateHolidaysUseCase: UpdateHolidaysUseCase
) : ViewModel() {
    var state by mutableStateOf(HomeState())
        private set

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                state = state.copy(currentProfile = profile)
                if (profile == null) return@collectLatest
                getDayUseCase(profile, state.currentTime.date).collectLatest { day ->
                    state = state.copy(currentDay = day)
                }
            }
        }
        viewModelScope.launch {
            getCurrentDateTimeUseCase().collect { time ->
                state = state.copy(currentTime = time)
            }
        }
    }

    private fun update() {
        state = state.copy(isUpdating = true)
        viewModelScope.launch {
            updateHolidaysUseCase(state.currentProfile!!.school as School.IndiwareSchool)
            updateTimetableUseCase(state.currentProfile!!.school as School.IndiwareSchool)
            updateSubstitutionPlanUseCase(state.currentProfile!!.school as School.IndiwareSchool, state.currentTime.date)
        }.invokeOnCompletion { state = state.copy(isUpdating = false) }
    }

    fun onEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                HomeEvent.OnRefresh -> update()
            }
        }
    }
}

data class HomeState(
    val currentProfile: Profile? = null,
    val currentTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val currentDay: SchoolDay? = null,
    val isUpdating: Boolean = false
)

sealed class HomeEvent {
    data object OnRefresh : HomeEvent()
}