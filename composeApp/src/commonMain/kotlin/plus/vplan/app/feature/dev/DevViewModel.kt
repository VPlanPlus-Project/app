package plus.vplan.app.feature.dev

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
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
                homeworkRepository.getByGroup((it as Profile.StudentProfile).group.id).collectLatest {
                    state = state.copy(homework = it)
                }
            }
        }
    }

    fun onEvent(event: DevEvent) {
        viewModelScope.launch {
            when (event) {
                DevEvent.Refresh -> updateHomeworkUseCase(state.profile as Profile.StudentProfile)
            }
        }
    }
}

data class DevState(
    val homework: List<Homework> = emptyList(),
    val profile: Profile? = null
)

sealed class DevEvent {
    data object Refresh : DevEvent()
}