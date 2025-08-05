package plus.vplan.app.feature.onboarding.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.feature.onboarding.domain.model.OnboardingSp24State
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.lib.sp24.source.IndiwareClient

interface OnboardingRepository {
    suspend fun reset()
    suspend fun startSp24Onboarding(
        sp24Id: Int,
    )

    fun setSp24Client(indiwareClient: IndiwareClient)
    fun getSp24Client(): IndiwareClient?

    suspend fun setSp24Credentials(
        username: String,
        password: String
    )

    suspend fun addProfileOptions(options: List<OnboardingProfile>)

    suspend fun setSp24CredentialsValid(state: Sp24CredentialsState?)

    suspend fun setSchoolName(name: String?)

    suspend fun setSelectedProfile(option: OnboardingProfile)

    fun getState(): Flow<OnboardingSp24State>
}

enum class Sp24CredentialsState {
    NOT_CHECKED,
    LOADING,
    VALID,
    INVALID,
    ERROR
}

fun Sp24CredentialsState.toButtonState(): ButtonState {
    return when (this) {
        Sp24CredentialsState.LOADING -> ButtonState.Loading
        else -> ButtonState.Enabled
    }
}