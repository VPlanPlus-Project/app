package plus.vplan.app.feature.dev.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import kotlin.uuid.Uuid

class DevViewModel(
    private val keyValueRepository: KeyValueRepository,
) : ViewModel() {
    var state by mutableStateOf(DevState())
        private set

    init {
        viewModelScope.launch {
            keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().collectLatest { profileId ->
                App.profileSource.getById(Uuid.parseHex(profileId))
                    .filterIsInstance<CacheState.Done<Profile>>()
                    .collectLatest { state = state.copy(profile = it.data) }
            }
        }
    }

    fun onEvent(event: DevEvent) {
        viewModelScope.launch {
            when (event) {
                DevEvent.Clear -> Unit
                DevEvent.Sync -> Unit
            }
        }
    }
}

data class DevState(
    val profile: Profile? = null
)

sealed class DevEvent {
    data object Clear : DevEvent()
    data object Sync : DevEvent()
}