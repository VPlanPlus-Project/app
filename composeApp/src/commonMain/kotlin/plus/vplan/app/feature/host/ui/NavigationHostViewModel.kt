package plus.vplan.app.feature.host.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.feature.host.domain.usecase.HasProfileUseCase

class NavigationHostViewModel(
    private val hasProfileUseCase: HasProfileUseCase
) : ViewModel() {
    var state by mutableStateOf(NavigationHostUiState())
        private set

    init {
        viewModelScope.launch {
            hasProfileUseCase().collectLatest { state = state.copy(hasProfile = it) }
        }
    }
}

data class NavigationHostUiState(
    val hasProfile: Boolean? = null
)