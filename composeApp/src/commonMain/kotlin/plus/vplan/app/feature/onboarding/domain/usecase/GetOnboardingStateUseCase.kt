package plus.vplan.app.feature.onboarding.domain.usecase

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.feature.onboarding.domain.model.OnboardingSp24State
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository

class GetOnboardingStateUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    operator fun invoke(): Flow<OnboardingSp24State> {
        return onboardingRepository.getState()
    }
}