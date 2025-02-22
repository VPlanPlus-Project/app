package plus.vplan.app.feature.onboarding.stage.e_permissions.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.usecase.OnNotificationGrantedUseCase

class OnboardingPermissionViewModel(
    private val onNotificationGrantedUseCase: OnNotificationGrantedUseCase
) : ViewModel() {
    var state by mutableStateOf(OnboardingPermissionState())
        private set

    fun onNotificationGranted() {
        viewModelScope.launch {
            onNotificationGrantedUseCase()
            state = OnboardingPermissionState(canContinue = true)
        }
    }
}

data class OnboardingPermissionState(
    val canContinue: Boolean = false
)