package plus.vplan.app.feature.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase

class HomeViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase
) : ViewModel() {
    var state by mutableStateOf(HomeState())
        private set

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collect { profile ->
                state = state.copy(currentProfile = profile)
            }
        }
        viewModelScope.launch {
            getCurrentDateTimeUseCase().collect { time ->
                state = state.copy(currentTime = time)
            }
        }
    }
}

data class HomeState(
    val currentProfile: Profile? = null,
    val currentTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)