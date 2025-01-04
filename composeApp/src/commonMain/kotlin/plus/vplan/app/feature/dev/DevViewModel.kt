package plus.vplan.app.feature.dev

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase

class DevViewModel(
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val homeworkRepository: HomeworkRepository
) : ViewModel() {
    var state by mutableStateOf(DevState())
        private set

    init {
        viewModelScope.launch {
            App.homeworkSource.getById(
                id = 208.toString(),
                configuration = Homework.Fetch(
                    vppId = VppId.Fetch()
                )
            ).collect {
                state = state.copy(homework = listOf(it))
            }
        }
    }

    fun onEvent(event: DevEvent) {
        viewModelScope.launch {
            when (event) {
                DevEvent.Refresh -> Unit
                DevEvent.Clear -> homeworkRepository.clearCache()
            }
        }
    }
}

data class DevState(
    val reloadResponse: Response<Unit>? = null,
    val homework: List<Cacheable<Homework>> = emptyList(),
)

sealed class DevEvent {
    data object Refresh : DevEvent()
    data object Clear : DevEvent()
}