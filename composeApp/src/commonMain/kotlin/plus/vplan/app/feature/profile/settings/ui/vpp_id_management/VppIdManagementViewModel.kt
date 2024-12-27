package plus.vplan.app.feature.profile.settings.ui.vpp_id_management

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.feature.profile.settings.domain.usecase.LogoutVppIdUseCase

class VppIdManagementViewModel(
    private val logoutVppIdUseCase: LogoutVppIdUseCase
) : ViewModel() {
    var state by mutableStateOf(VppIdManagementState())
        private set

    fun init(vppId: VppId.Active) {
        state = state.copy(vppId = vppId)
    }

    fun onEvent(event: VppIdManagementEvent) {
        viewModelScope.launch {
            when (event) {
                VppIdManagementEvent.Logout -> {
                    state = state.copy(deletionState = Response.Loading)
                    state = state.copy(deletionState = logoutVppIdUseCase(state.vppId!!))
                }
            }
        }
    }
}

data class VppIdManagementState(
    val vppId: VppId.Active? = null,
    val deletionState: Response<Unit>? = null
)

sealed class VppIdManagementEvent {
    data object Logout : VppIdManagementEvent()
}