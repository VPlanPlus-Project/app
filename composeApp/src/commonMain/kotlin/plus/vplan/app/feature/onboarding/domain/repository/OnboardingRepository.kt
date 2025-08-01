package plus.vplan.app.feature.onboarding.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.ProfileType
import plus.vplan.app.feature.onboarding.domain.model.OnboardingSp24State
import plus.vplan.app.ui.components.ButtonState

interface OnboardingRepository {
    suspend fun reset()
    suspend fun startSp24Onboarding(
        sp24Id: Int,
    )

    suspend fun setSp24Credentials(
        username: String,
        password: String
    )

    suspend fun setSp24CredentialsValid(state: Sp24CredentialsState?)

    suspend fun setSchoolName(name: String?)

    suspend fun setSelectedProfile(
        type: ProfileType,
        name: String
    )

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