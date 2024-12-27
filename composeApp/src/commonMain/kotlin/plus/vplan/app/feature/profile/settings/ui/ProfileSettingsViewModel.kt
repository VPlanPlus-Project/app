package plus.vplan.app.feature.profile.settings.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetProfileByIdUseCase
import plus.vplan.app.feature.profile.settings.domain.usecase.CheckIfVppIdIsStillConnectedUseCase
import plus.vplan.app.feature.profile.settings.domain.usecase.RenameProfileUseCase
import plus.vplan.app.feature.profile.settings.domain.usecase.VppIdConnectionState
import kotlin.uuid.Uuid

class ProfileSettingsViewModel(
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val renameProfileUseCase: RenameProfileUseCase,
    private val checkIfVppIdIsStillConnectedUseCase: CheckIfVppIdIsStillConnectedUseCase
) : ViewModel() {
    var state by mutableStateOf(ProfileSettingsState())
        private set

    fun init(profileId: String) {
        viewModelScope.launch {
            getProfileByIdUseCase(Uuid.parse(profileId)).collect { profile ->
                state = state.copy(profile = profile)
                if (profile is Profile.StudentProfile && profile.vppId != null) {
                    checkIfVppIdIsStillConnectedUseCase(profile.vppId).let {
                        state = state.copy(isVppIdStillConnected = it)
                    }
                }
            }
        }
    }

    fun onEvent(event: ProfileSettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is ProfileSettingsEvent.RenameProfile -> renameProfileUseCase(state.profile!!, event.newName)
            }
        }
    }
}

data class ProfileSettingsState(
    val profile: Profile? = null,
    val isVppIdStillConnected: VppIdConnectionState = VppIdConnectionState.UNKNOWN
)

sealed class ProfileSettingsEvent {
    data class RenameProfile(val newName: String) : ProfileSettingsEvent()
}