package plus.vplan.app.feature.onboarding.stage.permissions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plus.vplan.app.core.platform.PermissionRepository

class PermissionsViewModel(
    private val permissionRepository: PermissionRepository,
) : ViewModel() {
    val state: StateFlow<PermissionsState>
        field = MutableStateFlow(PermissionsState())

    init {
        viewModelScope.launch(Dispatchers.Default + CoroutineName("${this::class.simpleName}.Init.IsGranted")) {
            if (permissionRepository.isGranted(Permission.REMOTE_NOTIFICATION)) {
                state.update { state -> state.copy(isDone = true) }
            }
        }
    }

    fun onEvent(event: PermissionsEvent) {
        when (event) {
            is PermissionsEvent.Request -> {
                viewModelScope.launch(Dispatchers.Default + CoroutineName("${this::class.simpleName}.Event.Request")) {
                    permissionRepository.request(Permission.REMOTE_NOTIFICATION)
                    state.update { state -> state.copy(isDone = true) }
                }
            }
        }
    }
}

data class PermissionsState(
    val isDone: Boolean = false,
)

sealed class PermissionsEvent {
    data object Request: PermissionsEvent()
}