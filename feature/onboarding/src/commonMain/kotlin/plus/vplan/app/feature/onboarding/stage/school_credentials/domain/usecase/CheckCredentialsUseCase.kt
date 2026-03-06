package plus.vplan.app.feature.onboarding.stage.school_credentials.domain.usecase

import plus.vplan.app.core.data.stundenplan24.Stundenplan24Repository
import plus.vplan.app.core.model.NetworkException
import plus.vplan.lib.sp24.source.Authentication

class CheckCredentialsUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
) {
    suspend operator fun invoke(sp24Id: Int, username: String, password: String): Sp24LookupResult {
        val authentication = Authentication(sp24Id.toString(), username, password)
        return try {
            val valid = stundenplan24Repository.checkCredentials(authentication)
//            capture("Onboarding.CredentialsProvided", mapOf(
//                "sp24Id" to sp24Id,
//                "username" to username,
//                "password" to password
//            ))
            if (valid) {
                Sp24LookupResult.UseSchool(sp24Id)
            } else {
                Sp24LookupResult.WrongCredentials
            }
        } catch (e: NetworkException) {
            Sp24LookupResult.NetworkError(e)
        }
    }
}

sealed class Sp24LookupResult {
    data class UseSchool(val sp24Id: Int) : Sp24LookupResult()
    data object WrongCredentials : Sp24LookupResult()
    data class NetworkError(val exception: NetworkException) : Sp24LookupResult()
}
