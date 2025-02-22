package plus.vplan.app.feature.profile.settings.page.main.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.usecase.GetProfileByIdUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.CheckIfVppIdIsStillConnectedUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.RenameProfileUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.VppIdConnectionState
import kotlin.uuid.Uuid

private val logger = Logger.withTag("ProfileSettingsViewModel")

class ProfileSettingsViewModel(
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val renameProfileUseCase: RenameProfileUseCase,
    private val checkIfVppIdIsStillConnectedUseCase: CheckIfVppIdIsStillConnectedUseCase,
) : ViewModel() {
    var state by mutableStateOf(ProfileSettingsState())
        private set

    fun init(profileId: String) {
        viewModelScope.launch {
            logger.d { "Init profile settings for profile $profileId" }
            getProfileByIdUseCase(Uuid.parse(profileId)).collect { profile ->
                logger.d { "Got profile $profile" }
                state = state.copy(profile = profile.also { it?.prefetch() })
                if (profile is Profile.StudentProfile && profile.vppId != null) {
                    checkIfVppIdIsStillConnectedUseCase(App.vppIdSource.getById(profile.vppId).getFirstValue() as VppId.Active).let {
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

private suspend fun Profile.prefetch() {
    this.getSchoolItem()
    if (this is Profile.StudentProfile) this.getVppIdItem()
}