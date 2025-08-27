package plus.vplan.app.feature.search.subfeature.room_search.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.domain.model.LessonTime
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
    var state by mutableStateOf(RoomSearchState())
        private set

    init {
        viewModelScope.launch { getCurrentDateTimeUseCase().collectLatest { state = state.copy(currentTime = it) } }
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { currentProfile ->
                state = state.copy(currentProfile = currentProfile)
                combine(
                    getRoomOccupationMapUseCase(currentProfile, LocalDate.now()),
                    getLessonTimesForProfileUseCase(currentProfile)
                ) { map, lessonTimes ->
                    state = state.copy(rooms = map, lessonTimes = lessonTimes.associateWith { false }, initDone = true)
                }.collect()
            }
        }
    }

    fun onEvent(event: RoomSearchEvent) {
        viewModelScope.launch {
            when (event) {
                is RoomSearchEvent.ToggleLessonTimeSelection -> state = state.copy(lessonTimes = state.lessonTimes.plus(event.lessonTime to (state.lessonTimes[event.lessonTime]?.not() ?: false)))
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