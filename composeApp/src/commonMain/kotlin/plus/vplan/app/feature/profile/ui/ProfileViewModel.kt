package plus.vplan.app.feature.profile.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase

class ProfileViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) : ViewModel() {
    var state by mutableStateOf(ProfileState())
        private set

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collect { profile ->
                state = state.copy(currentProfile = profile)
            }
        }
    }
}

data class ProfileState(
    val currentProfile: Profile? = null,
    val profiles: List<Profile> = emptyList()
)