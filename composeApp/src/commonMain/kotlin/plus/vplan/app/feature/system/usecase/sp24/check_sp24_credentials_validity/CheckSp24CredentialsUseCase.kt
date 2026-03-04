package plus.vplan.app.feature.system.usecase.sp24.check_sp24_credentials_validity

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.data.stundenplan24.Stundenplan24Repository
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.School
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client
import plus.vplan.lib.sp24.source.TestConnectionResult

class CheckSp24CredentialsUseCase(
    private val schoolRepository: SchoolRepository,
    private val stundenplan24Repository: Stundenplan24Repository
) {
    suspend operator fun invoke(
        client: Stundenplan24Client?,
        authentication: Authentication
    ): Response<Sp24CredentialsValidity> {
        val client = client ?: stundenplan24Repository.getSp24Client(authentication = authentication, withCache = true)

        when (val response = client.testConnection()) {
            is TestConnectionResult.Error -> return Response.Error.fromSp24KtError(response.error)
            is TestConnectionResult.Success -> return Response.Success(Sp24CredentialsValidity.Valid)
            is TestConnectionResult.NotFound -> return Response.Error.Other("School ${authentication.sp24SchoolId} does not exist")
            is TestConnectionResult.Unauthorized -> {
                val school = schoolRepository.getById(
                    Alias(
                        provider = AliasProvider.Sp24,
                        value = authentication.sp24SchoolId,
                        version = 1
                    )
                ).first() as? School.AppSchool
                    ?: return Response.Success(Sp24CredentialsValidity.Invalid.InvalidFirstTime)

                if (!school.credentialsValid && school.username == authentication.username && school.password == authentication.password) {
                    return Response.Success(Sp24CredentialsValidity.Invalid.StillInvalid)
                }

                return Response.Success(Sp24CredentialsValidity.Invalid.InvalidFirstTime)
            }
        }
    }
}

sealed class Sp24CredentialsValidity {
    data object Valid : Sp24CredentialsValidity()
    sealed class Invalid : Sp24CredentialsValidity() {
        data object InvalidFirstTime : Invalid()
        data object StillInvalid : Invalid()
    }
}