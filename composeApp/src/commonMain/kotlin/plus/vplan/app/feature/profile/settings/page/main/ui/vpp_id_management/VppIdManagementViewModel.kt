package plus.vplan.app.feature.profile.settings.page.main.ui.vpp_id_management

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdDevice
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.GetVppIdDevicesUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.LogoutVppIdDeviceUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.LogoutVppIdUseCase

class VppIdManagementViewModel(
    private val logoutVppIdUseCase: LogoutVppIdUseCase,
    private val logoutVppIdDeviceUseCase: LogoutVppIdDeviceUseCase,
    private val getVppIdDevicesUseCase: GetVppIdDevicesUseCase
) : ViewModel() {
    var state by mutableStateOf(VppIdManagementState())
        private set

    fun init(vppId: VppId.Active) {
        state = state.copy(vppId = vppId)
        viewModelScope.launch {
            state = state.copy(devices = getVppIdDevicesUseCase(vppId))
        }
    }

    fun onEvent(event: VppIdManagementEvent) {
        viewModelScope.launch {
            when (event) {
                VppIdManagementEvent.Logout -> {
                    state = state.copy(logoutState = Response.Loading)
                    state = state.copy(logoutState = logoutVppIdUseCase(state.vppId!!))
                }
                is VppIdManagementEvent.LogoutDevice -> {
                    val devices = (state.devices as? Response.Success)?.data?.toMutableList() ?: return@launch
                    devices.removeAll { it.id == event.device.id }
                    state = state.copy(devices = Response.Success(devices))
                    logoutVppIdDeviceUseCase(state.vppId!!, event.device)
                }
            }
        }
    }
}

data class VppIdManagementState(
    val vppId: VppId.Active? = null,
    val logoutState: Response<Unit>? = null,
    val devices: Response<List<VppIdDevice>> = Response.Loading
)

sealed class VppIdManagementEvent {
    data object Logout : VppIdManagementEvent()
    data class LogoutDevice(val device: VppIdDevice) : VppIdManagementEvent()
}