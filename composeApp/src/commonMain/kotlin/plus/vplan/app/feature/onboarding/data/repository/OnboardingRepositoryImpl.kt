package plus.vplan.app.feature.onboarding.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import plus.vplan.app.feature.onboarding.domain.model.OnboardingSp24State
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.domain.repository.Sp24CredentialsState
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.app.feature.sync.domain.usecase.fullsync.FullSyncUseCase
import plus.vplan.lib.sp24.source.Stundenplan24Client

class OnboardingRepositoryImpl: OnboardingRepository {

    private var onboardingSp24State: MutableStateFlow<OnboardingSp24State?> = MutableStateFlow(null)
    private var stundenplan24Client: Stundenplan24Client? = null

    override suspend fun reset() {
        onboardingSp24State.value = null
    }

    override fun setSp24Client(stundenplan24Client: Stundenplan24Client) {
        this.stundenplan24Client = stundenplan24Client
    }

    override fun getSp24Client(): Stundenplan24Client? {
        return stundenplan24Client
    }

    override suspend fun startSp24Onboarding(sp24Id: Int) {
        FullSyncUseCase.isOnboardingRunning = true
        onboardingSp24State.update { OnboardingSp24State(sp24Id = sp24Id) }
    }

    override suspend fun setSp24Credentials(username: String, password: String) {
        onboardingSp24State.update {
            it!!.copy(
                username = username,
                password = password
            )
        }
    }

    override suspend fun setSp24CredentialsValid(state: Sp24CredentialsState?) {
        onboardingSp24State.update {
            it!!.copy(
                sp24CredentialsState = state ?: Sp24CredentialsState.NOT_CHECKED
            )
        }
    }

    override fun getState(): Flow<OnboardingSp24State> {
        return onboardingSp24State.filterNotNull()
    }

    override suspend fun setSchoolName(name: String?) {
        onboardingSp24State.update {
            it!!.copy(
                schoolName = name
            )
        }
    }

    override suspend fun addProfileOptions(options: List<OnboardingProfile>) {
        onboardingSp24State.update {
            it!!.copy(profileOptions = it.profileOptions + options)
        }
    }

    override suspend fun setSelectedProfile(option: OnboardingProfile) {
        onboardingSp24State.update {
            it!!.copy(
                selectedItem = option
            )
        }
    }
}
