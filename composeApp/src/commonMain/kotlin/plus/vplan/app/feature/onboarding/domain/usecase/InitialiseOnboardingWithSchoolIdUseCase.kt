package plus.vplan.app.feature.onboarding.domain.usecase

import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository

class InitialiseOnboardingWithSchoolIdUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke(schoolId: Int?) {
        onboardingRepository.clear()
        schoolId?.let { onboardingRepository.setSchoolId(it) }
    }
}