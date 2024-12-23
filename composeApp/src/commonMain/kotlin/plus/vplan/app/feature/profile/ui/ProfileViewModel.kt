package plus.vplan.app.feature.profile.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.domain.usecase.SetCurrentProfileUseCase
import plus.vplan.app.feature.profile.domain.usecase.GetProfilesUseCase

class ProfileViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val setCurrentProfileUseCase: SetCurrentProfileUseCase,
    private val getProfilesUseCase: GetProfilesUseCase
) : ViewModel() {
    var state by mutableStateOf(ProfileState())
        private set

    init {
        viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                getProfilesUseCase()
            ) { currentProfile, profiles ->
                state.copy(currentProfile = currentProfile, profiles = profiles)
            }
            .collect { state = it }
        }
    }

    fun onEvent(event: ProfileScreenEvent) {
        viewModelScope.launch {
            when (event) {
                is ProfileScreenEvent.SetProfileSwitcherVisibility -> state = state.copy(isSheetVisible = event.to)
                is ProfileScreenEvent.SetActiveProfile -> setCurrentProfileUseCase(event.profile)
            }
        }
    }
}

data class ProfileState(
    val currentProfile: Profile? = null,
    val profiles: Map<School, List<Profile>> = emptyMap(),

    val isSheetVisible: Boolean = false
)

sealed class ProfileScreenEvent {
    data class SetProfileSwitcherVisibility(val to: Boolean): ProfileScreenEvent()
    data class SetActiveProfile(val profile: Profile): ProfileScreenEvent()
}