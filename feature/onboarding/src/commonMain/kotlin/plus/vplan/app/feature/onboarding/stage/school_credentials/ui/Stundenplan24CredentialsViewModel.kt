package plus.vplan.app.feature.onboarding.stage.school_credentials.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plus.vplan.app.core.ui.components.ButtonState
import plus.vplan.app.feature.onboarding.stage.school_credentials.domain.usecase.CheckCredentialsUseCase
import plus.vplan.app.feature.onboarding.stage.school_credentials.domain.usecase.Sp24LookupResult

class Stundenplan24CredentialsViewModel(
    private val checkCredentialsUseCase: CheckCredentialsUseCase,
) : ViewModel() {

    val state: StateFlow<Stundenplan24CredentialsState>
        field = MutableStateFlow(Stundenplan24CredentialsState())

    init {
        viewModelScope.launch {
        }
    }

    fun handleEvent(event: Stundenplan24CredentialsEvent) {
        when (event) {
            is Stundenplan24CredentialsEvent.OnUsernameChanged -> state.update { state ->
                state.copy(
                    username = event.username
                )
            }

            is Stundenplan24CredentialsEvent.OnPasswordChanged -> state.update { state ->
                state.copy(
                    password = event.password
                )
            }

            is Stundenplan24CredentialsEvent.OnCheckClicked -> {
                viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".Action.Check")) {
                    state.update { state -> state.copy(sp24CredentialsState = Sp24CredentialsState.LOADING) }
                    val result = checkCredentialsUseCase(
                        state.value.sp24Id!!,
                        state.value.username,
                        state.value.password
                    )
                    when (result) {
                        is Sp24LookupResult.NetworkError -> state.update { state ->
                            state.copy(
                                sp24CredentialsState = Sp24CredentialsState.ERROR
                            )
                        }

                        is Sp24LookupResult.WrongCredentials -> state.update { state ->
                            state.copy(
                                sp24CredentialsState = Sp24CredentialsState.INVALID
                            )
                        }

                        is Sp24LookupResult.UseSchool -> {
                            state.update { state ->
                                state.copy(
                                    sp24CredentialsState = Sp24CredentialsState.VALID,
                                    isThisStageFinished = true,
                                )
                            }
                        }
                    }
                }
            }
            is Stundenplan24CredentialsEvent.OnScreenBecameActive -> {
                state.update { state -> state.copy(isThisStageFinished = false) }
            }
        }
    }

    fun init(sp24Id: Int?) {
        state.update { Stundenplan24CredentialsState(sp24Id = sp24Id) }
    }
}

data class Stundenplan24CredentialsState(
    val sp24Id: Int? = null,
    val username: String = "schueler",
    val password: String = "",
    val sp24CredentialsState: Sp24CredentialsState = Sp24CredentialsState.NOT_CHECKED,
    val isThisStageFinished: Boolean = false,
) {
    val isUsernameValid: Boolean
        get() = username in listOf("schueler", "lehrer")
}

sealed class Stundenplan24CredentialsEvent {
    data class OnUsernameChanged(val username: String) : Stundenplan24CredentialsEvent()
    data class OnPasswordChanged(val password: String) : Stundenplan24CredentialsEvent()
    data object OnCheckClicked : Stundenplan24CredentialsEvent()
    data object OnScreenBecameActive : Stundenplan24CredentialsEvent()
}

enum class Sp24CredentialsState {
    NOT_CHECKED,
    LOADING,
    VALID,
    INVALID,
    ERROR
}

fun Sp24CredentialsState.toButtonState(): ButtonState {
    return when (this) {
        Sp24CredentialsState.LOADING -> ButtonState.Loading
        else -> ButtonState.Enabled
    }
}