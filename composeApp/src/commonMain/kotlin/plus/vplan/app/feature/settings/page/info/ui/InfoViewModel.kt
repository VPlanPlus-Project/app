package plus.vplan.app.feature.settings.page.info.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys

class InfoViewModel(
    private val keyValueRepository: KeyValueRepository
) : ViewModel() {

    fun handleEvent(event: InfoEvent) {
        viewModelScope.launch {
            when (event) {
                is InfoEvent.EnableDeveloperMode -> {
                    keyValueRepository.set(Keys.DEVELOPER_SETTINGS_ACTIVE, "true")
                }
            }
        }
    }
}

sealed class InfoEvent {
    object EnableDeveloperMode : InfoEvent()
}