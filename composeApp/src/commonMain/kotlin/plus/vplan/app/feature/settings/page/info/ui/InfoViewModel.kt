package plus.vplan.app.feature.settings.page.info.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.model.application.AppPlatform
import plus.vplan.app.core.platform.PlatformRepository

class InfoViewModel(
    private val keyValueRepository: KeyValueRepository,
    platformRepository: PlatformRepository
) : ViewModel() {

    val state: StateFlow<InfoState>
    field = MutableStateFlow(InfoState(
        platform = platformRepository.getPlatform()
    ))
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

data class InfoState(
    val platform: AppPlatform = AppPlatform.Android
)

sealed class InfoEvent {
    object EnableDeveloperMode : InfoEvent()
}
