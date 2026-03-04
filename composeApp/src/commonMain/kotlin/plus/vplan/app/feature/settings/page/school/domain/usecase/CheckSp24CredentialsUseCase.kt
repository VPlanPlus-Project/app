package plus.vplan.app.feature.settings.page.school.domain.usecase

import plus.vplan.app.core.data.stundenplan24.Stundenplan24Repository
import plus.vplan.app.core.model.NetworkException
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsCredentialsState
import plus.vplan.lib.sp24.source.Authentication

class CheckSp24CredentialsUseCase(
    private val stundenplan24Repository: Stundenplan24Repository
){
    suspend operator fun invoke(sp24Id: Int, username: String, password: String): SchoolSettingsCredentialsState {
        return try {
            val valid = stundenplan24Repository.checkCredentials(Authentication(sp24Id.toString(), username, password))
            if (valid) SchoolSettingsCredentialsState.Valid else SchoolSettingsCredentialsState.Invalid
        } catch (e: NetworkException) {
            SchoolSettingsCredentialsState.Error
        }
    }
}
