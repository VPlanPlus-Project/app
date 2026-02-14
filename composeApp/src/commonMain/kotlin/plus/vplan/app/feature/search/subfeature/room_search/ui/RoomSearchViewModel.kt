package plus.vplan.app.feature.search.subfeature.room_search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.GetLessonTimesForProfileUseCase
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.GetRoomOccupationMapUseCase
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.OccupancyMapRecord
import plus.vplan.app.utils.now

class RoomSearchViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getRoomOccupationMapUseCase: GetRoomOccupationMapUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getLessonTimesForProfileUseCase: GetLessonTimesForProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RoomSearchState())
    val state: StateFlow<RoomSearchState> = _state

    init {
        viewModelScope.launch {
            getCurrentDateTimeUseCase().collectLatest { currentTime ->
                _state.update { it.copy(currentTime = currentTime) }
            }
        }
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { currentProfile ->
                _state.update { it.copy(currentProfile = currentProfile) }
                combine(
                    getRoomOccupationMapUseCase(currentProfile, LocalDate.now()),
                    getLessonTimesForProfileUseCase(currentProfile)
                ) { map, lessonTimes ->
                    _state.update { oldState ->
                        oldState.copy(rooms = map, lessonTimes = lessonTimes.associateWith { oldState.lessonTimes[it] ?: false }, initDone = true)
                    }
                }.collect()
            }
        }
    }

    fun onEvent(event: RoomSearchEvent) {
        viewModelScope.launch {
            when (event) {
                is RoomSearchEvent.ToggleLessonTimeSelection -> {
                    _state.update {
                        it.copy(lessonTimes = it.lessonTimes.plus(event.lessonTime to (it.lessonTimes[event.lessonTime]?.not() ?: false)))
                    }
                }
            }
        }
    }
}

data class RoomSearchState(
    val rooms: List<OccupancyMapRecord> = emptyList(),
    val initDone: Boolean = false,
    val currentProfile: Profile? = null,
    val lessonTimes: Map<LessonTime, Boolean> = emptyMap(),
    val currentTime: LocalDateTime = LocalDateTime.now()
) {
    val startTime = rooms.map { it.occupancies }.flatten().minOfOrNull { it.start }
    val endTime = rooms.map { it.occupancies }.flatten().maxOfOrNull { it.end }
}

sealed class RoomSearchEvent {
    data class ToggleLessonTimeSelection(val lessonTime: LessonTime): RoomSearchEvent()
}