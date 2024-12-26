package plus.vplan.app.feature.vpp_id.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.feature.vpp_id.domain.usecase.AddVppIdUseCase

class VppIdSetupViewModel(
    private val addVppIdUseCase: AddVppIdUseCase
) : ViewModel() {
    var state by mutableStateOf(VppIdSetupState())
        private set

    fun init(token: String) {
        viewModelScope.launch {
            val response = addVppIdUseCase(token)
            state = state.copy(user = response)
        }
    }
}

data class VppIdSetupState(
    val user: Response<VppId.Active> = Response.Loading
)