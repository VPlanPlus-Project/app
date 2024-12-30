package plus.vplan.app.feature.homework.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import kotlin.uuid.Uuid

class NewHomeworkViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) : ViewModel() {
    var state by mutableStateOf(NewHomeworkState())
        private set

    init {
        viewModelScope.launch { getCurrentProfileUseCase().collect { state = state.copy(currentProfile = it as? Profile.StudentProfile) } }
    }

    fun onEvent(event: NewHomeworkEvent) {
        viewModelScope.launch {
            when (event) {
                is NewHomeworkEvent.AddTask -> state = state.copy(tasks = state.tasks.plus(Uuid.random() to event.task))
                is NewHomeworkEvent.UpdateTask -> state = state.copy(tasks = state.tasks.plus(event.taskId to event.task))
                is NewHomeworkEvent.RemoveTask -> state = state.copy(tasks = state.tasks.minus(event.taskId))
                is NewHomeworkEvent.SelectDefaultLesson -> state = state.copy(selectedDefaultLesson = event.defaultLesson)
                is NewHomeworkEvent.SelectDate -> state = state.copy(selectedDate = event.date)
                is NewHomeworkEvent.SetVisibility -> state = state.copy(isPublic = event.isPublic)
            }
        }
    }
}

data class NewHomeworkState(
    val tasks: Map<Uuid, String> = emptyMap(),
    val currentProfile: Profile.StudentProfile? = null,
    val selectedDefaultLesson: DefaultLesson? = null,
    val selectedDate: LocalDate? = null,
    val isPublic: Boolean = true
)

sealed class NewHomeworkEvent {
    data class AddTask(val task: String) : NewHomeworkEvent()
    data class UpdateTask(val taskId: Uuid, val task: String) : NewHomeworkEvent()
    data class RemoveTask(val taskId: Uuid) : NewHomeworkEvent()

    data class SelectDefaultLesson(val defaultLesson: DefaultLesson?) : NewHomeworkEvent()
    data class SelectDate(val date: LocalDate) : NewHomeworkEvent()

    data class SetVisibility(val isPublic: Boolean) : NewHomeworkEvent()
}