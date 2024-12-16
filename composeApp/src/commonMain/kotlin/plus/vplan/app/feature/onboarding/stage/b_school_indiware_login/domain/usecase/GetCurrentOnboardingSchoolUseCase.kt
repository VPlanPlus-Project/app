package plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.feature.onboarding.domain.repository.CurrentOnboardingSchool
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository

class GetCurrentOnboardingSchoolUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke(): Flow<CurrentOnboardingSchool?> {
        return onboardingRepository.getSp24OnboardingSchool()
    }
}