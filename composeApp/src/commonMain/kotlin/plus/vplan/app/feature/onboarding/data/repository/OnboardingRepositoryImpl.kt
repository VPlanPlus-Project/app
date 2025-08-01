package plus.vplan.app.feature.onboarding.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.ProfileType
import plus.vplan.app.feature.onboarding.domain.model.OnboardingSp24State
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.domain.repository.Sp24CredentialsState

class OnboardingRepositoryImpl: OnboardingRepository {

    private var onboardingSp24State: MutableStateFlow<OnboardingSp24State?> = MutableStateFlow(null)

    init {
        GlobalScope.launch {
            onboardingSp24State.collect {
                Logger.d { "Onboarding state: $it" }
            }
        }
    }

    override suspend fun reset() {
        onboardingSp24State.value = null
    }

    override suspend fun startSp24Onboarding(sp24Id: Int) {
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

    override suspend fun setSelectedProfile(type: ProfileType, name: String) {
        onboardingSp24State.update {
            it!!.copy(
                selectedItemType = type,
                selectedItem = name
            )
        }
    }
}
