package plus.vplan.app.feature.settings.page.developer.flags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys

class DeveloperFlagsViewModel(
    private val keyValueRepository: KeyValueRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DeveloperFlagsState())
    val state = _state.asStateFlow()
    init {
        viewModelScope.launch {
            Keys.developerSettings.forEach { developerFlag ->
                when (developerFlag) {
                    is Keys.DeveloperFlag.Boolean -> {
                        keyValueRepository.get(developerFlag.key).collectLatest { value ->
                            _state.update { it.copy(booleans = it.booleans.plus(developerFlag.key to value.toBoolean())) }
                        }
                    }
                }
            }
        }
    }

    fun onEvent(event: DeveloperFlagsEvent) {
        viewModelScope.launch {
            when (event) {
                is DeveloperFlagsEvent.Toggle -> {
                    val currentState = _state.value.booleans[event.key] ?: return@launch
                    keyValueRepository.set(event.key, (!currentState).toString())
                }
            }
        }
    }
}

data class DeveloperFlagsState(
    val booleans: Map<String, Boolean> = emptyMap()
)

sealed class DeveloperFlagsEvent {
    data class Toggle(val key: String): DeveloperFlagsEvent()
}