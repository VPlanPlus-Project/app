package plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.lib.sp24.source.Authentication

class CheckCredentialsUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val onboardingRepository: OnboardingRepository,
    private val schoolRepository: SchoolRepository
) {
    suspend operator fun invoke(sp24Id: Int, username: String, password: String): Response<Sp24LookupResponse> {
        onboardingRepository.clearSp24Credentials()
        onboardingRepository.setSp24CredentialsValid(Sp24CredentialsState.LOADING)
        val result = indiwareRepository.checkCredentials(Authentication(sp24Id.toString(), username, password))
        if (result is Response.Success) {
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
        val schools = schoolRepository.fetchAllOnline()
        if (schools is Response.Error) {
            return schools
        }
        schools as Response.Success
        if (schools.data.any { it.sp24Id == sp24Id }) {
            return Response.Success(Sp24LookupResponse.ExistingSchool(sp24Id, schools.data.first { it.sp24Id == sp24Id }.id, schools.data.first { it.sp24Id == sp24Id }.name, username, password))
        }
        return Response.Success(Sp24LookupResponse.FirstSchool(sp24Id))
    }
}

sealed class Sp24LookupResponse {
    data class FirstSchool(val sp24Id: Int): Sp24LookupResponse()
    data object WrongCredentials: Sp24LookupResponse()
    data class ExistingSchool(
        val sp24Id: Int,
        val schoolId: Int,
        val schoolName: String,
        val username: String,
        val password: String
    ) : Sp24LookupResponse()
}