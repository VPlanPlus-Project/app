package plus.vplan.app.feature.profile.settings.page.main.ui.vpp_id_management

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.core.data.vpp_id.VppIdDevice
import plus.vplan.app.core.model.VppId
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
            try {
                state = state.copy(devices = getVppIdDevicesUseCase(vppId), devicesLoading = false)
            } catch (e: Exception) {
                state = state.copy(devicesLoading = false, devicesError = true)
            }
        }
    }

    fun onEvent(event: VppIdManagementEvent) {
        viewModelScope.launch {
            when (event) {
                VppIdManagementEvent.Logout -> {
                    state = state.copy(isLoggingOut = true, logoutError = false)
                    try {
                        logoutVppIdUseCase(state.vppId!!)
                        state = state.copy(isLoggingOut = false, loggedOut = true)
                    } catch (e: Exception) {
                        state = state.copy(isLoggingOut = false, logoutError = true)
                    }
                }
                is VppIdManagementEvent.LogoutDevice -> {
                    val devices = state.devices.toMutableList()
                    devices.removeAll { it.id == event.device.id }
                    state = state.copy(devices = devices)
                    logoutVppIdDeviceUseCase(state.vppId!!, event.device)
                }
            }
        }
    }
}

data class VppIdManagementState(
    val vppId: VppId.Active? = null,
    val devices: List<VppIdDevice> = emptyList(),
    val devicesLoading: Boolean = true,
    val devicesError: Boolean = false,
    val isLoggingOut: Boolean = false,
    val logoutError: Boolean = false,
    val loggedOut: Boolean = false,
)

sealed class VppIdManagementEvent {
    data object Logout : VppIdManagementEvent()
    data class LogoutDevice(val device: VppIdDevice) : VppIdManagementEvent()
}
