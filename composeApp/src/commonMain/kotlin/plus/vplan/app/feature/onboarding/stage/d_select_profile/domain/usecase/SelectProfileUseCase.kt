package plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase

import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile

class SelectProfileUseCase(
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(
        onboardingProfile: OnboardingProfile,
        subjectInstances: Map<SubjectInstance, Boolean> = emptyMap()
    ) {
        onboardingRepository.setSelectedProfile(onboardingProfile.type, onboardingProfile.name)
    }
}