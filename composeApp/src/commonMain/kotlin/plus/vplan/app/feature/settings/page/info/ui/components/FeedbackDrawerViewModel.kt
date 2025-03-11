package plus.vplan.app.feature.settings.page.info.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.CheckEMailStructureUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.settings.page.info.domain.usecase.GetSystemInfoUseCase
import plus.vplan.app.feature.settings.page.info.domain.usecase.SystemInfo

class FeedbackDrawerViewModel(
    private val getSystemInfoUseCase: GetSystemInfoUseCase,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val checkEMailStructureUseCase: CheckEMailStructureUseCase
) : ViewModel() {
    var state by mutableStateOf(FeedbackDrawerState())
        private set

    init {
        init()
    }

    fun init() {
        state = FeedbackDrawerState()
        getSystemInfoUseCase().let { state = state.copy(systemInfo = it) }
        viewModelScope.launch { getCurrentProfileUseCase().collectLatest { state = state.copy(currentProfile = it) } }
    }

    fun onEvent(event: FeedbackEvent) {
        viewModelScope.launch {
            when (event) {
                is FeedbackEvent.UpdateMessage -> {
                    state = state.copy(message = event.message)
                    if (event.message.isNotBlank()) state = state.copy(showEmptyError = false)
                }
                is FeedbackEvent.UpdateEmail -> {
                    state = state.copy(customEmail = event.email)
                    if (event.email.isNotBlank()) state = state.copy(showEmailError = false)
                }
                is FeedbackEvent.RequestSend -> {
                    if (state.isLoading) return@launch
                    var error = false
                    if (state.message.isBlank()) {
                        state = state.copy(showEmptyError = true)
                        error = true
                    }
                    if (state.customEmail.isNotBlank() && !checkEMailStructureUseCase(state.customEmail)) {
                        state = state.copy(showEmailError = true)
                        error = true
                    }
                    if (error) return@launch
                    state = state.copy(showEmptyError = false, showEmailError = false, isLoading = true)
                    delay(1000)
                    state = state.copy(sendDone = true)
                }
            }
        }
    }
}

data class FeedbackDrawerState(
    val showEmptyError: Boolean = false,
    val message: String = "",
    var systemInfo: SystemInfo? = null,
    val currentProfile: Profile? = null,
    val customEmail: String = "",
    val showEmailError: Boolean = false,
    val isLoading: Boolean = false,
    val sendDone: Boolean = false
)

sealed class FeedbackEvent {
    data class UpdateMessage(val message: String): FeedbackEvent()
    data class UpdateEmail(val email: String): FeedbackEvent()

    data object RequestSend: FeedbackEvent()
}