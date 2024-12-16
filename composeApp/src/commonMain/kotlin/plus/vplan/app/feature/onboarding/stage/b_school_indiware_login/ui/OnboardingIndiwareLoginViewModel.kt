package plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.CheckCredentialsUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.GetCurrentOnboardingSchoolUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.GetSp24CredentialsStateUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.Sp24CredentialsState

class OnboardingIndiwareLoginViewModel(
    private val checkCredentialsUseCase: CheckCredentialsUseCase,
    private val getCurrentOnboardingSchoolUseCase: GetCurrentOnboardingSchoolUseCase,
    private val getSp24CredentialsStateUseCase: GetSp24CredentialsStateUseCase
) : ViewModel() {
    var state by mutableStateOf<OnboardingIndiwareLoginState?>(null)
        private set

    init {
        viewModelScope.launch {
            combine(
                getCurrentOnboardingSchoolUseCase(),
                getSp24CredentialsStateUseCase()
            ) { currentOnboardingSchool, sp24CredentialsState ->
                state?.copy(
                    sp24Id = currentOnboardingSchool?.sp24Id ?: return@combine null,
                    schoolName = currentOnboardingSchool.schoolName,
                    sp24CredentialsState = sp24CredentialsState
                ) ?: OnboardingIndiwareLoginState(
                    sp24Id = currentOnboardingSchool?.sp24Id ?: 0,
                    schoolName = currentOnboardingSchool?.schoolName
                )
            }.collect {
                state = it
            }
        }
    }

    fun handleEvent(event: OnboardingIndiwareLoginEvent) {
        viewModelScope.launch {
            when (event) {
                is OnboardingIndiwareLoginEvent.OnUsernameChanged -> state = state?.copy(username = event.username)
                is OnboardingIndiwareLoginEvent.OnPasswordChanged -> state = state?.copy(password = event.password)
                is OnboardingIndiwareLoginEvent.OnCheckClicked -> checkCredentialsUseCase(state!!.sp24Id, state!!.username, state!!.password)
            }
        }
    }
}

data class OnboardingIndiwareLoginState(
    val sp24Id: Int,
    val schoolName: String? = null,
    val username: String = "schueler",
    val password: String = "",
    val sp24CredentialsState: Sp24CredentialsState = Sp24CredentialsState.NOT_CHECKED
)

sealed class OnboardingIndiwareLoginEvent {
    data class OnUsernameChanged(val username: String) : OnboardingIndiwareLoginEvent()
    data class OnPasswordChanged(val password: String) : OnboardingIndiwareLoginEvent()
    data object OnCheckClicked : OnboardingIndiwareLoginEvent()
}