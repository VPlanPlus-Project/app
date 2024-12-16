package plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository

class CheckCredentialsUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke(sp24Id: Int, username: String, password: String): Response<Boolean> {
        onboardingRepository.clearSp24Credentials()
        onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.LOADING)
        val result = indiwareRepository.checkCredentials(sp24Id, username, password)
        if (result is Response.Success) {
            onboardingRepository.setSp24Credentials(username, password)
            if (result.data) onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.VALID)
            else onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.INVALID)
        } else {
            onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.ERROR)
        }
        return result
    }
}