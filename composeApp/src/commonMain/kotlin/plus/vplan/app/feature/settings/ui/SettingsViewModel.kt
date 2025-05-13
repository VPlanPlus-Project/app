package plus.vplan.app.feature.settings.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys

class SettingsViewModel(
    private val keyValueRepository: KeyValueRepository
) : ViewModel() {
    var state by mutableStateOf(SettingsState())
        private set

    init {
        viewModelScope.launch {
            keyValueRepository.get(Keys.DEVELOPER_SETTINGS_ACTIVE).map { it == "true" }.collectLatest {
                state = state.copy(isDeveloperSettingsEnabled = it)
            }
        }
    }
}

data class SettingsState(
    val isDeveloperSettingsEnabled: Boolean = false
)