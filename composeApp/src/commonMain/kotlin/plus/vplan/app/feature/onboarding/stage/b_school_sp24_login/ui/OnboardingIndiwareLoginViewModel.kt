package plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import plus.vplan.app.domain.data.Response
import plus.vplan.app.feature.onboarding.domain.repository.Sp24CredentialsState
import plus.vplan.app.feature.onboarding.domain.usecase.GetOnboardingStateUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.domain.usecase.CheckCredentialsUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.domain.usecase.Sp24LookupResponse
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen

class OnboardingIndiwareLoginViewModel(
    private val checkCredentialsUseCase: CheckCredentialsUseCase,
    private val getOnboardingStateUseCase: GetOnboardingStateUseCase
) : ViewModel() {
    var state by mutableStateOf<OnboardingIndiwareLoginState?>(null)
        private set

    private lateinit var navController: NavHostController

    fun init(navController: NavHostController) {
        this.navController = navController
    }

    init {
        viewModelScope.launch {
            getOnboardingStateUseCase().collect { onboardingState ->
                if (onboardingState.sp24Id == null) return@collect
                state = if (state == null) {
                    OnboardingIndiwareLoginState(
                        sp24Id = onboardingState.sp24Id,
                        schoolName = onboardingState.schoolName,
                        sp24CredentialsState = onboardingState.sp24CredentialsState
                    )
                } else state?.copy(
                    sp24Id = onboardingState.sp24Id,
                    schoolName = onboardingState.schoolName,
                    sp24CredentialsState = onboardingState.sp24CredentialsState
                )
            }
        }
    }

    fun handleEvent(event: OnboardingIndiwareLoginEvent) {
        viewModelScope.launch {
            when (event) {
                is OnboardingIndiwareLoginEvent.OnUsernameChanged -> state = state?.copy(username = event.username)
                is OnboardingIndiwareLoginEvent.OnPasswordChanged -> state = state?.copy(password = event.password)
                is OnboardingIndiwareLoginEvent.OnCheckClicked -> {
                    val result = checkCredentialsUseCase(state!!.sp24Id, state!!.username, state!!.password)
                    if (result is Response.Success) {
                        when (result.data) {
                            is Sp24LookupResponse.UseSchool -> {
                                navController.navigate(OnboardingScreen.OnboardingIndiwareDataDownload)
                            }
                            else -> {}
                        }
                    }
                }
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
) {
    val isUsernameValid: Boolean
        get() = username in listOf("schueler", "lehrer")
}

sealed class OnboardingIndiwareLoginEvent {
    data class OnUsernameChanged(val username: String) : OnboardingIndiwareLoginEvent()
    data class OnPasswordChanged(val password: String) : OnboardingIndiwareLoginEvent()
    data object OnCheckClicked : OnboardingIndiwareLoginEvent()
}