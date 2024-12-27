package plus.vplan.app.feature.profile.settings.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetProfileByIdUseCase
import kotlin.uuid.Uuid

class ProfileSettingsViewModel(
    private val getProfileByIdUseCase: GetProfileByIdUseCase
) : ViewModel() {
    var state by mutableStateOf(ProfileSettingsState())
        private set

    fun init(profileId: String) {
        viewModelScope.launch {
            getProfileByIdUseCase(Uuid.parse(profileId)).collect { profile ->
                state = state.copy(profile = profile)
            }
        }
    }

    fun onEvent(event: ProfileSettingsEvent) {

    }
}

data class ProfileSettingsState(
    val profile: Profile? = null
)

sealed class ProfileSettingsEvent {

}