package plus.vplan.app.feature.dev.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase
import kotlin.uuid.Uuid

class DevViewModel(
    private val homeworkRepository: HomeworkRepository,
    private val keyValueRepository: KeyValueRepository,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase
) : ViewModel() {
    var state by mutableStateOf(DevState())
        private set

    init {
        viewModelScope.launch {
            keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().collectLatest {
                App.profileSource.getById(Uuid.parseHex(it))
                    .filterIsInstance<CacheState.Done<Profile>>()
                    .collectLatest { state = state.copy(profile = it.data) }
            }
        }

        viewModelScope.launch {
            App.homeworkSource.getAll().map { it.filterIsInstance<CacheState.Done<Homework>>().map { it.data } }.collect {
                state = state.copy(homework = it.onEachIndexed { index, homework ->
                    homework.prefetch()
                })
            }
        }
    }

    fun onEvent(event: DevEvent) {
        viewModelScope.launch {
            when (event) {
                DevEvent.Refresh -> {
                    Logger.d { "Homework update started" }
                    updateHomeworkUseCase()
                    Logger.d { "Homework updated" }
                }

                DevEvent.Clear -> homeworkRepository.clearCache()
            }
        }
    }
}

data class DevState(
    val profile: Profile? = null,
    val homework: List<Homework> = emptyList(),
    val updateResponse: Response.Error? = null
)

sealed class DevEvent {
    data object Refresh : DevEvent()
    data object Clear : DevEvent()
}

private suspend fun Homework.prefetch() {
    this.getDefaultLessonItem()
    this.getTaskItems()
    if (this is Homework.CloudHomework) {
        this.getCreatedBy()
        this.getGroupItem()
    }
    if (this is Homework.LocalHomework) {
        this.getCreatedByProfile().getGroupItem()
    }
}