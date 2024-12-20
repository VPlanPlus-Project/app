package plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.Sp24CredentialsState.LOADING
import plus.vplan.app.ui.components.ButtonState

class GetSp24CredentialsStateUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    operator fun invoke(): Flow<Sp24CredentialsState> {
        return onboardingRepository.getSp24CredentialsState()
    }
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
        LOADING -> ButtonState.LOADING
        else -> ButtonState.ENABLED
    }
}