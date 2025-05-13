package plus.vplan.app.feature.settings.page.developer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.feature.sync.domain.usecase.FullSyncCause
import plus.vplan.app.feature.sync.domain.usecase.FullSyncUseCase

class DeveloperSettingsViewModel(
    private val fullSyncUseCase: FullSyncUseCase
) : ViewModel() {

    var state by mutableStateOf(DeveloperSettingsState())

    fun handleEvent(event: DeveloperSettingsEvent) {
        viewModelScope.launch {
            when (event) {
                DeveloperSettingsEvent.StartFullSync -> {
                    if (state.isFullSyncRunning) return@launch
                    state = state.copy(isFullSyncRunning = true)
                    launch { fullSyncUseCase(FullSyncCause.Manual).join() }.invokeOnCompletion {
                        state = state.copy(isFullSyncRunning = false)
                    }
                }
            }
        }
    }
}

data class DeveloperSettingsState(
    val isFullSyncRunning: Boolean = false
)

sealed class DeveloperSettingsEvent {
    object StartFullSync : DeveloperSettingsEvent()
}