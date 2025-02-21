package plus.vplan.app.feature.settings.page.school.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.settings.page.school.domain.usecase.CheckSp24CredentialsUseCase
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsCredentialsState

class IndiwareCredentialViewModel(
    private val schoolRepository: SchoolRepository,
    private val checkSp24CredentialsUseCase: CheckSp24CredentialsUseCase
): ViewModel() {
    var state by mutableStateOf<IndiwareCredentialState?>(null)
        private set

    fun init(schoolId: Int) {
        viewModelScope.launch {
            val school = schoolRepository.getById(schoolId).getFirstValue() as? School.IndiwareSchool ?: return@launch
            state = IndiwareCredentialState(
                school = school,
                schoolName = school.name,
                sp24Id = school.sp24Id,
                username = school.username,
                password = school.password,
                state = null
            )
        }
    }

    fun onEvent(event: IndiwareCredentialEvent) {
        when (event) {
            is IndiwareCredentialEvent.SetUsername -> state = state?.copy(username = event.username)
            is IndiwareCredentialEvent.SetPassword -> state = state?.copy(password = event.password)
            is IndiwareCredentialEvent.Save -> {
                viewModelScope.launch {
                    state = state?.copy(state = SchoolSettingsCredentialsState.Loading)
                    delay(1000)
                    val result = checkSp24CredentialsUseCase(state!!.sp24Id.toInt(), state!!.username, state!!.password)
                    if (result == SchoolSettingsCredentialsState.Valid) {
                        schoolRepository.updateSp24Access(state!!.school, state!!.username, state!!.password)
                        schoolRepository.setIndiwareAccessValidState(state!!.school, true)
                    }
                    state = state?.copy(state = result)
                }
            }
        }
    }
}

data class IndiwareCredentialState(
    val school: School.IndiwareSchool,
    val schoolName: String,
    val sp24Id: String,
    val username: String,
    val password: String,
    val state: SchoolSettingsCredentialsState? = null
)

sealed class IndiwareCredentialEvent {
    data class SetUsername(val username: String) : IndiwareCredentialEvent()
    data class SetPassword(val password: String) : IndiwareCredentialEvent()

    data object Save : IndiwareCredentialEvent()
}