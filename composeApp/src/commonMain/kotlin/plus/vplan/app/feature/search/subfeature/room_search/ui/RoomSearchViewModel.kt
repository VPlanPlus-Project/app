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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.GetLessonTimesForProfileUseCase
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.GetRoomOccupationMapUseCase
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.Occupancy

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
                    getRoomOccupationMapUseCase(currentProfile, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date),
                    getLessonTimesForProfileUseCase(currentProfile)
                ) { map, lessonTimes ->
                    state = state.copy(rooms = map, lessonTimes = lessonTimes, initDone = true)
                }.collect()
            }
        }
    }
}

data class RoomSearchState(
    val rooms: Map<Room, Set<Occupancy>> = emptyMap(),
    val initDone: Boolean = false,
    val currentProfile: Profile? = null,
    val lessonTimes: List<LessonTime> = emptyList(),
    val currentTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
) {
    val startTime = rooms.values.flatten().minOfOrNull { it.start }
    val endTime = rooms.values.flatten().maxOfOrNull { it.end }
}