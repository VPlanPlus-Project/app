package plus.vplan.app.feature.settings.page.school.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsCredentialsState

class CheckSp24CredentialsUseCase(
    private val indiwareRepository: IndiwareRepository
){
    suspend operator fun invoke(sp24Id: Int, username: String, password: String): SchoolSettingsCredentialsState {
        indiwareRepository.checkCredentials(sp24Id, username, password).let {
            return when (it) {
                is Response.Success -> if (it.data) SchoolSettingsCredentialsState.Valid else SchoolSettingsCredentialsState.Invalid
                else -> SchoolSettingsCredentialsState.Error
            }
        }
    }
}