package plus.vplan.app.feature.settings.page.school.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.School
import plus.vplan.app.feature.settings.page.school.domain.usecase.CheckSp24CredentialsUseCase
import plus.vplan.app.feature.settings.page.school.domain.usecase.GetSchoolsUseCase

class SchoolSettingsViewModel(
    private val getSchoolsUseCase: GetSchoolsUseCase,
    private val checkSp24CredentialsUseCase: CheckSp24CredentialsUseCase
) : ViewModel() {
    var state by mutableStateOf(SchoolSettingsState())
        private set

    init {
        viewModelScope.launch {
            getSchoolsUseCase().collectLatest { schools ->
                state = state.copy(
                    schools = schools.map {
                        SchoolSettingsSchool(
                            school = it,
                            credentialsState = SchoolSettingsCredentialsState.Loading
                        )
                    }
                )

                state.schools.orEmpty().map { it.school }.filterIsInstance<School.AppSchool>().forEach { school ->
                    viewModelScope.launch {
                        checkSp24CredentialsUseCase(school.sp24Id.toInt(), school.username, school.password).let { credentialsState ->
                            state = state.copy(
                                schools = state.schools.orEmpty().map { schoolSettingsSchool ->
                                    if (schoolSettingsSchool.school == school) schoolSettingsSchool.copy(credentialsState = credentialsState) else schoolSettingsSchool
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class SchoolSettingsState(
    val schools: List<SchoolSettingsSchool>? = null,
)

data class SchoolSettingsSchool(
    val school: School,
    val credentialsState: SchoolSettingsCredentialsState,
)

enum class SchoolSettingsCredentialsState {
    Loading,
    Valid,
    Invalid,
    Error
}