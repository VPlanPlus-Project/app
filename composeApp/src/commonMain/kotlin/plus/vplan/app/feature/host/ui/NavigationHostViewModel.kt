package plus.vplan.app.feature.host.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import plus.vplan.app.feature.host.domain.usecase.HasProfileUseCase
import plus.vplan.app.feature.main.domain.usecase.SetupApplicationUseCase

class NavigationHostViewModel(
    private val hasProfileUseCase: HasProfileUseCase,
    private val setupApplicationUseCase: SetupApplicationUseCase
) : ViewModel() {
    var state by mutableStateOf(NavigationHostUiState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(hasProfileAtAppStartup = hasProfileUseCase().first())
        }

        viewModelScope.launch {
            hasProfileUseCase().collect { hasProfiles ->
                if (hasProfiles) setupApplicationUseCase()
            }
        }
    }
}

data class NavigationHostUiState(
    val hasProfileAtAppStartup: Boolean? = null
)