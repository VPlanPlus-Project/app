package plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository

class StartIndiwareInitJobUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke(): Response<String> {
        return onboardingRepository.startSp24UpdateJob()
    }
}