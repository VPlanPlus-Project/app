package plus.vplan.app.feature.vpp_id.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.VppId
import plus.vplan.app.feature.vpp_id.domain.usecase.AddVppIdUseCase

class VppIdSetupViewModel(
    private val addVppIdUseCase: AddVppIdUseCase
) : ViewModel() {
    var state by mutableStateOf(VppIdSetupState())
        private set

    fun init(token: String) {
        state = VppIdSetupState(isLoading = true)
        viewModelScope.launch {
            try {
                val vppId = addVppIdUseCase(token)
                state = state.copy(isLoading = false, user = vppId)
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = true)
            }
        }
    }
}

data class VppIdSetupState(
    val isLoading: Boolean = true,
    val user: VppId.Active? = null,
    val error: Boolean = false,
)
