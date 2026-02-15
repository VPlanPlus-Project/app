package plus.vplan.app.feature.profile.settings.page.main.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.usecase.GetProfileByIdUseCase
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.CheckIfVppIdIsStillConnectedUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.ConnectVppIdUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.DeleteProfileUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.IsLastProfileOfSchoolUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.RenameProfileUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.VppIdConnectionState
import kotlin.uuid.Uuid

private val logger = Logger.withTag("ProfileSettingsViewModel")

class ProfileSettingsViewModel(
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val renameProfileUseCase: RenameProfileUseCase,
    private val checkIfVppIdIsStillConnectedUseCase: CheckIfVppIdIsStillConnectedUseCase,
    private val isLastProfileOfSchoolUseCase: IsLastProfileOfSchoolUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val connectVppIdUseCase: ConnectVppIdUseCase,
    private val vppIdRepository: VppIdRepository
) : ViewModel() {
    var state by mutableStateOf(ProfileSettingsState())
        private set

    fun init(profileId: String) {
        viewModelScope.launch {
            state = state.copy(profileDeletionState = null)
            logger.d { "Init profile settings for profile $profileId" }
            getProfileByIdUseCase(Uuid.parse(profileId)).collectLatest { profile ->
                logger.d { "Got profile $profile" }
                state = state.copy(profile = profile)
                if (profile is Profile.StudentProfile && profile.vppId != null) {
                    checkIfVppIdIsStillConnectedUseCase(vppIdRepository.getById(profile.vppId.id, ResponsePreference.Fast).getFirstValueOld() as VppId.Active).let {
                        state = state.copy(isVppIdStillConnected = it)
                    }
                }

                if (profile != null) isLastProfileOfSchoolUseCase(profile).collectLatest { state = state.copy(isLastProfileOfSchool = it) }
            }
        }
    }

    fun onEvent(event: ProfileSettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is ProfileSettingsEvent.RenameProfile -> renameProfileUseCase(state.profile!!, event.newName)
                is ProfileSettingsEvent.DeleteProfile -> {
                    state = state.copy(profileDeletionState = UnoptimisticTaskState.InProgress)
                    deleteProfileUseCase(state.profile!!)
                    state = state.copy(profileDeletionState = UnoptimisticTaskState.Success)
                }
                is ProfileSettingsEvent.ConnectVppId -> connectVppIdUseCase(state.profile as Profile.StudentProfile)
            }
        }
    }
}

data class ProfileSettingsState(
    val profile: Profile? = null,
    val profileDeletionState: UnoptimisticTaskState? = null,
    val isLastProfileOfSchool: Boolean = false,
    val isVppIdStillConnected: VppIdConnectionState = VppIdConnectionState.UNKNOWN
)

sealed class ProfileSettingsEvent {
    data class RenameProfile(val newName: String) : ProfileSettingsEvent()
    data object DeleteProfile : ProfileSettingsEvent()

    data object ConnectVppId: ProfileSettingsEvent()
}