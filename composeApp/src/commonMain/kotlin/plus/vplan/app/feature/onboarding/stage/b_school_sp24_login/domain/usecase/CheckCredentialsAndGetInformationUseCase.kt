package plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.domain.usecase

import plus.vplan.app.capture
import plus.vplan.app.core.data.stundenplan24.Stundenplan24Repository
import plus.vplan.app.core.model.NetworkException
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.domain.repository.Sp24CredentialsState
import plus.vplan.lib.sp24.source.Authentication

class CheckCredentialsUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(sp24Id: Int, username: String, password: String): Sp24LookupResult {
        onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.LOADING)
        val authentication = Authentication(sp24Id.toString(), username, password)
        return try {
            val valid = stundenplan24Repository.checkCredentials(authentication)
            capture("Onboarding.CredentialsProvided", mapOf(
                "sp24Id" to sp24Id,
                "username" to username,
                "password" to password
            ))
            onboardingRepository.setSp24Client(stundenplan24Repository.getSp24Client(authentication, withCache = true))
            onboardingRepository.setSp24Credentials(username, password)
            if (valid) {
                onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.VALID)
                Sp24LookupResult.UseSchool(sp24Id)
            } else {
                onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.INVALID)
                Sp24LookupResult.WrongCredentials
            }
        } catch (e: NetworkException) {
            onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.ERROR)
            Sp24LookupResult.NetworkError(e)
        }
    }
}

sealed class Sp24LookupResult {
    data class UseSchool(val sp24Id: Int) : Sp24LookupResult()
    data object WrongCredentials : Sp24LookupResult()
    data class NetworkError(val exception: NetworkException) : Sp24LookupResult()
}

/** Kept for source-compatibility with existing callers that use Sp24LookupResponse */
typealias Sp24LookupResponse = Sp24LookupResult
