package plus.vplan.app.feature.settings.page.developer.timetable_debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.domain.model.Timetable
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase

class TimetableDebugViewModel(
    private val weekRepository: WeekRepository,
    private val timetableRepository: TimetableRepository,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(TimetableDebugState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                val school = profile.getSchool().getFirstValue()!!
                weekRepository.getBySchool(school.id).collectLatest collectWeek@{ weeks ->
                    weeks.forEach { week ->
                        launch {
                            timetableRepository.getTimetableData(school.id, week.id).collectLatest { timetableMetadata ->
                                val currentState = _state.value
                                _state.value = currentState.copy(
                                    weeks = (currentState.weeks + TimetableDebugState.Week(
                                        week = week,
                                        timetableMetadata = timetableMetadata
                                    )).sortedBy { it.week.weekIndex }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TimetableDebugState(
    val weeks: List<Week> = emptyList(),
) {
    data class Week(
        val week: plus.vplan.app.domain.model.Week,
        val timetableMetadata: Timetable?
    )
}