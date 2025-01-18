package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.homework.domain.usecase.ToggleTaskDoneUseCase

class DetailViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val toggleTaskDoneUseCase: ToggleTaskDoneUseCase
) : ViewModel() {
    var state by mutableStateOf(DetailState())
        private set

    fun init(homeworkId: Int) {
        viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                App.homeworkSource.getById(homeworkId)
            ) { profile, homeworkData ->
                if (homeworkData !is CacheState.Done || profile !is Profile.StudentProfile) return@combine null
                val homework = homeworkData.data
                homework.getDefaultLessonItem()
                homework.getGroupItem()
                if (homework is Homework.CloudHomework) homework.getCreatedBy()
                state.copy(homework = homework, profile = profile)
            }.filterNotNull().collectLatest { state = it }
        }
    }

    fun onEvent(event: DetailEvent) {
        viewModelScope.launch {
            when (event) {
                is DetailEvent.ToggleTaskDone -> toggleTaskDoneUseCase(event.task, state.profile!!)
            }
        }
    }
}

data class DetailState(
    val homework: Homework? = null,
    val profile: Profile.StudentProfile? = null
)

sealed class DetailEvent {
    data class ToggleTaskDone(val task: Homework.HomeworkTask) : DetailEvent()
}