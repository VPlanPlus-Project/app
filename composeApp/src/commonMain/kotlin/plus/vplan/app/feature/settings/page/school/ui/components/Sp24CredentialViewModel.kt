package plus.vplan.app.feature.settings.page.school.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.School
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.settings.page.school.domain.usecase.CheckSp24CredentialsUseCase
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsCredentialsState
import plus.vplan.app.feature.system.usecase.sp24.SendSp24CredentialsToServerUseCase
import kotlin.uuid.Uuid

class Sp24CredentialViewModel(
    private val schoolRepository: SchoolRepository,
    private val checkSp24CredentialsUseCase: CheckSp24CredentialsUseCase,
    private val sendSp24CredentialsToServerUseCase: SendSp24CredentialsToServerUseCase
): ViewModel() {
    var state by mutableStateOf<Sp24CredentialState?>(null)
        private set

    fun init(schoolId: Uuid) {
        viewModelScope.launch {
            val school = schoolRepository.getByLocalId(schoolId).first() as? School.AppSchool ?: return@launch
            state = Sp24CredentialState(
                school = school,
                schoolName = school.name,
                sp24Id = school.sp24Id,
                username = school.username,
                password = school.password,
                state = null
            )
        }
    }

    fun onEvent(event: Sp24CredentialEvent) {
        when (event) {
            is Sp24CredentialEvent.SetUsername -> state = state?.copy(username = event.username)
            is Sp24CredentialEvent.SetPassword -> state = state?.copy(password = event.password)
            is Sp24CredentialEvent.Save -> {
                viewModelScope.launch {
                    state = state?.copy(state = SchoolSettingsCredentialsState.Loading)
                    delay(1000)
                    val result = checkSp24CredentialsUseCase(state!!.sp24Id.toInt(), state!!.username, state!!.password)
                    if (result == SchoolSettingsCredentialsState.Valid) {
                        schoolRepository.setSp24Access(
                            schoolId = state!!.school.id,
                            sp24Id = state!!.school.sp24Id.toInt(),
                            username = state!!.username,
                            password = state!!.password,
                            daysPerWeek = state!!.school.daysPerWeek,
                        )
                        schoolRepository.setSp24CredentialValidity(state!!.school.id, true)
                        sendSp24CredentialsToServerUseCase()
                    }
                    state = state?.copy(state = result)
                }
            }
        }
    }
}

data class Sp24CredentialState(
    val school: School.AppSchool,
    val schoolName: String,
    val sp24Id: String,
    val username: String,
    val password: String,
    val state: SchoolSettingsCredentialsState? = null
)

sealed class Sp24CredentialEvent {
    data class SetUsername(val username: String) : Sp24CredentialEvent()
    data class SetPassword(val password: String) : Sp24CredentialEvent()

    data object Save : Sp24CredentialEvent()
}