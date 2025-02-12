package plus.vplan.app.feature.search.subfeature.room_search.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.GetRoomOccupationMapUseCase
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.Occupancy

class RoomSearchViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getRoomOccupationMapUseCase: GetRoomOccupationMapUseCase
) : ViewModel() {
    var state by mutableStateOf(RoomSearchState())
        private set

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { currentProfile ->
                state = state.copy(currentProfile = currentProfile)
                getRoomOccupationMapUseCase(currentProfile, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date).collectLatest { map ->
                    state = state.copy(rooms = map)
                }
            }
        }
    }
}

data class RoomSearchState(
    val rooms: Map<Room, Set<Occupancy>> = emptyMap(),
    val currentProfile: Profile? = null
) {
    val startTime = rooms.values.flatten().minOfOrNull { it.start }
    val endTime = rooms.values.flatten().maxOfOrNull { it.end }
}