package plus.vplan.app.feature.settings.page.school.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsCredentialsState
import plus.vplan.lib.sp24.source.Authentication

class CheckSp24CredentialsUseCase(
    private val stundenplan24Repository: Stundenplan24Repository
){
    suspend operator fun invoke(sp24Id: Int, username: String, password: String): SchoolSettingsCredentialsState {
        stundenplan24Repository.checkCredentials(Authentication(sp24Id.toString(), username, password)).let {
            return when (it) {
                is Response.Success -> if (it.data) SchoolSettingsCredentialsState.Valid else SchoolSettingsCredentialsState.Invalid
                else -> SchoolSettingsCredentialsState.Error
            }
        }
    }
}