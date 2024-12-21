package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.feature.sync.domain.usecase.UpdateWeeksUseCase

class HomeViewModel(
    private val weekRepository: WeekRepository,
    private val schoolRepository: SchoolRepository,
    private val updateWeeksUseCase: UpdateWeeksUseCase
) : ViewModel() {
    var state by mutableStateOf(HomeUiState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(school = schoolRepository.getById(67).first())
            weekRepository.getBySchool(schoolId = 67).collect { weeks ->
                state = state.copy(weeks = weeks.sortedBy { it.weekIndex })
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                HomeUiEvent.UpdateWeeks -> updateWeeksUseCase(state.school as School.IndiwareSchool)
                is HomeUiEvent.DeleteWeeks -> {
                    if (event.all) weekRepository.deleteBySchool(schoolId = state.school?.id ?: return@launch)
                    else state.weeks.filter { it.weekType == "A" }.forEach { weekRepository.deleteById(it.id) }
                }
                HomeUiEvent.SneakWeekIn -> weekRepository.upsert(Week(
                    id = "67/000",
                    calendarWeek = 1,
                    start = LocalDate.parse("2022-01-01"),
                    end = LocalDate.parse("2022-01-07"),
                    weekType = "C",
                    weekIndex = 1,
                    school = state.school as School.IndiwareSchool
                ))
            }
        }
    }
}

data class HomeUiState(
    val school: School? = null,
    val weeks: List<Week> = emptyList()
)

sealed class HomeUiEvent {
    data object UpdateWeeks : HomeUiEvent()
    data class DeleteWeeks(val all: Boolean) : HomeUiEvent()
    data object SneakWeekIn : HomeUiEvent()
}