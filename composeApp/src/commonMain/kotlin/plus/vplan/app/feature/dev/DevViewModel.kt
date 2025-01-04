package plus.vplan.app.feature.dev

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase

class DevViewModel(
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val homeworkRepository: HomeworkRepository
) : ViewModel() {
    var state by mutableStateOf(DevState())
        private set

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest {
                state = state.copy(profile = it)
//                homeworkRepository.getByGroup((it as Profile.StudentProfile).group.id).collectLatest {
//                    state = state.copy(homework = it)
//                }
                App.homeworkSource.getById(
                    id = 208,
                    configuration = Homework.Fetch(
                        vppId = VppId.Fetch()
                    )
                ).collect {
                    state = state.copy(homework = listOf(it))
                }
            }
        }
    }

    fun onEvent(event: DevEvent) {
        viewModelScope.launch {
            when (event) {
                DevEvent.Refresh -> {
                    val result = updateHomeworkUseCase(state.profile as Profile.StudentProfile)
                    state = state.copy(reloadResponse = result)
                }
                DevEvent.Clear -> homeworkRepository.clearCache()
            }
        }
    }
}

data class DevState(
    val reloadResponse: Response<Unit>? = null,
    val homework: List<Cacheable<Homework>> = emptyList(),
    val profile: Profile? = null
)

sealed class DevEvent {
    data object Refresh : DevEvent()
    data object Clear : DevEvent()
}