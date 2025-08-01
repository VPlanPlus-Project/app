package plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase

import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository

class SelectSp24SchoolUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke(sp24SchoolId: Int) {
        onboardingRepository.startSp24Onboarding(sp24SchoolId)
    }
}