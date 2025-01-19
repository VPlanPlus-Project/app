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
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkDefaultLessonUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkDueToUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkVisibilityUseCase
import plus.vplan.app.feature.homework.domain.usecase.ToggleTaskDoneUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateHomeworkUseCase

class DetailViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val toggleTaskDoneUseCase: ToggleTaskDoneUseCase,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val editHomeworkDefaultLessonUseCase: EditHomeworkDefaultLessonUseCase,
    private val editHomeworkDueToUseCase: EditHomeworkDueToUseCase,
    private val editHomeworkVisibilityUseCase: EditHomeworkVisibilityUseCase
) : ViewModel() {
    var state by mutableStateOf(DetailState())
        private set

    fun init(homeworkId: Int) {
        state = DetailState()
        viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                App.homeworkSource.getById(homeworkId)
            ) { profile, homeworkData ->
                if (homeworkData !is CacheState.Done || profile !is Profile.StudentProfile) return@combine null
                val homework = homeworkData.data

                homework.prefetch()
                profile.prefetch()

                state.copy(
                    homework = homework,
                    profile = profile,
                    canEdit = (homework is Homework.CloudHomework && homework.createdBy == profile.vppId) || (homework is Homework.LocalHomework && homework.createdByProfile == profile.id),
                    isReloading = false,
                    initDone = true
                )
            }.filterNotNull().collectLatest { state = it }
        }
    }

    fun onEvent(event: DetailEvent) {
        viewModelScope.launch {
            when (event) {
                is DetailEvent.ToggleTaskDone -> toggleTaskDoneUseCase(event.task, state.profile!!)
                is DetailEvent.UpdateDefaultLesson -> editHomeworkDefaultLessonUseCase(state.homework!!, event.defaultLesson, state.profile!!)
                is DetailEvent.UpdateDueTo -> editHomeworkDueToUseCase(state.homework!!, event.dueTo, state.profile!!)
                is DetailEvent.UpdateVisibility -> editHomeworkVisibilityUseCase(state.homework as Homework.CloudHomework, event.isPublic, state.profile!!)
                is DetailEvent.Reload -> {
                    state = state.copy(isReloading = true)
                    updateHomeworkUseCase(state.homework!!.id)
                }
            }
        }
    }
}

data class DetailState(
    val homework: Homework? = null,
    val profile: Profile.StudentProfile? = null,
    val canEdit: Boolean = false,
    val isReloading: Boolean = false,
    val initDone: Boolean = false
)

sealed class DetailEvent {
    data class ToggleTaskDone(val task: Homework.HomeworkTask) : DetailEvent()
    data class UpdateDefaultLesson(val defaultLesson: DefaultLesson?) : DetailEvent()
    data class UpdateDueTo(val dueTo: LocalDate) : DetailEvent()
    data class UpdateVisibility(val isPublic: Boolean) : DetailEvent()
    data object Reload : DetailEvent()
}

private suspend fun Profile.StudentProfile.prefetch() {
    this.getGroupItem()
    this.getDefaultLessons().onEach {
        it.getCourseItem()
        it.getTeacherItem()
    }
}

private suspend fun Homework.prefetch() {
    this.getGroupItem()
    this.getDefaultLessonItem()
    if (this is Homework.CloudHomework) this.getCreatedBy()
}