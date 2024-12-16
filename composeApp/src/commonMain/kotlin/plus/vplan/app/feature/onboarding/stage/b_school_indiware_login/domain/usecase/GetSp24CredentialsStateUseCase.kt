package plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository

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