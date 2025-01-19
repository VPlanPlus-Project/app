package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.homework.domain.usecase.AddTaskUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteTaskUseCase
import plus.vplan.app.feature.homework.domain.usecase.DownloadFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkDefaultLessonUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkDueToUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkVisibilityUseCase
import plus.vplan.app.feature.homework.domain.usecase.RenameFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.ToggleTaskDoneUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateTaskUseCase

class DetailViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val toggleTaskDoneUseCase: ToggleTaskDoneUseCase,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val editHomeworkDefaultLessonUseCase: EditHomeworkDefaultLessonUseCase,
    private val editHomeworkDueToUseCase: EditHomeworkDueToUseCase,
    private val editHomeworkVisibilityUseCase: EditHomeworkVisibilityUseCase,
    private val deleteHomeworkUseCase: DeleteHomeworkUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val renameFileUseCase: RenameFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase
) : ViewModel() {
    var state by mutableStateOf(DetailState())
        private set

    private var mainJob: Job? = null

    fun init(homeworkId: Int) {
        state = DetailState()
        mainJob?.cancel()
        mainJob = viewModelScope.launch {
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
                is DetailEvent.DeleteHomework -> {
                    state = state.copy(deleteState = UnoptimisticTaskState.InProgress)
                    val result = deleteHomeworkUseCase(state.homework!!, state.profile!!)
                    state = if (result) {
                        state.copy(deleteState = UnoptimisticTaskState.Success)
                    } else {
                        state.copy(deleteState = UnoptimisticTaskState.Error)
                    }
                }
                is DetailEvent.AddTask -> {
                    state = state.copy(newTaskState = UnoptimisticTaskState.InProgress)
                    val result = addTaskUseCase(state.homework!!, event.task, state.profile!!)
                    state = if (result) state.copy(newTaskState = UnoptimisticTaskState.Success) else state.copy(newTaskState = UnoptimisticTaskState.Error)
                }
                is DetailEvent.UpdateTask -> updateTaskUseCase(event.task, event.newContent, state.profile!!)
                is DetailEvent.DeleteTask -> {
                    if (state.homework!!.tasks.size == 1) return@launch onEvent(DetailEvent.DeleteHomework)
                    state = state.copy(taskDeleteState = state.taskDeleteState.plus(event.task.id to UnoptimisticTaskState.InProgress))
                    val result = deleteTaskUseCase(event.task, state.profile!!)
                    state = if (result) {
                        state.copy(taskDeleteState = state.taskDeleteState.plus(event.task.id to UnoptimisticTaskState.Success))
                    } else {
                        state.copy(taskDeleteState = state.taskDeleteState.plus(event.task.id to UnoptimisticTaskState.Error))
                    }
                }
                is DetailEvent.DownloadFile -> {
                    downloadFileUseCase(event.file, state.profile!!).collectLatest {
                        state = state.copy(fileDownloadState = state.fileDownloadState.plus(event.file.id to it))
                    }
                    state = state.copy(fileDownloadState = state.fileDownloadState - event.file.id)
                }
                is DetailEvent.RenameFile -> renameFileUseCase(event.file, event.newName, state.profile!!)
                is DetailEvent.DeleteFile -> deleteFileUseCase(event.file, state.profile!!)
            }
        }
    }
}

data class DetailState(
    val homework: Homework? = null,
    val profile: Profile.StudentProfile? = null,
    val canEdit: Boolean = false,
    val isReloading: Boolean = false,
    val deleteState: UnoptimisticTaskState? = null,
    val initDone: Boolean = false,
    val newTaskState: UnoptimisticTaskState? = null,
    val taskDeleteState: Map<Int, UnoptimisticTaskState> = emptyMap(),
    val fileDownloadState: Map<Int, Float> = emptyMap(),
)

sealed class DetailEvent {
    data class ToggleTaskDone(val task: Homework.HomeworkTask) : DetailEvent()
    data class UpdateDefaultLesson(val defaultLesson: DefaultLesson?) : DetailEvent()
    data class UpdateDueTo(val dueTo: LocalDate) : DetailEvent()
    data class UpdateVisibility(val isPublic: Boolean) : DetailEvent()
    data class AddTask(val task: String) : DetailEvent()
    data class UpdateTask(val task: Homework.HomeworkTask, val newContent: String) : DetailEvent()
    data class DeleteTask(val task: Homework.HomeworkTask) : DetailEvent()
    data object DeleteHomework : DetailEvent()
    data class DownloadFile(val file: File) : DetailEvent()

    data class RenameFile(val file: File, val newName: String) : DetailEvent()
    data class DeleteFile(val file: File) : DetailEvent()
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
    this.getFileItems()
    if (this is Homework.CloudHomework) this.getCreatedBy()
}

enum class UnoptimisticTaskState {
    InProgress, Error, Success
}