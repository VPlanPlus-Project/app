package plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.domain.usecase

import plus.vplan.app.capture
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.domain.repository.Sp24CredentialsState
import plus.vplan.lib.sp24.source.Authentication

class CheckCredentialsUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(sp24Id: Int, username: String, password: String): Response<Sp24LookupResponse> {
        onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.LOADING)
        val authentication = Authentication(sp24Id.toString(), username, password)
        val result = stundenplan24Repository.checkCredentials(authentication)
        if (result is Response.Success) {
            capture("Onboarding.CredentialsProvided", mapOf(
                "sp24Id" to sp24Id,
                "username" to username,
                "password" to password
            ))
            onboardingRepository.setSp24Client(stundenplan24Repository.getSp24Client(authentication, withCache = true))
            onboardingRepository.setSp24Credentials(username, password)
            if (result.data) onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.VALID)
            else {
                onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.INVALID)
                return Response.Success(Sp24LookupResponse.WrongCredentials)
            }
        } else {
            onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.ERROR)
            result as Response.Error
            return result
        }
        return Response.Success(Sp24LookupResponse.UseSchool(sp24Id))
    }
}

sealed class Sp24LookupResponse {
    data class UseSchool(val sp24Id: Int): Sp24LookupResponse()
    data object WrongCredentials: Sp24LookupResponse()
}