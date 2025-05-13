@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult
import plus.vplan.app.feature.homework.domain.usecase.AddFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.AddTaskUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteTaskUseCase
import plus.vplan.app.feature.homework.domain.usecase.DownloadFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkSubjectInstanceUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkDueToUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkVisibilityUseCase
import plus.vplan.app.feature.homework.domain.usecase.RenameFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.ToggleTaskDoneUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateTaskUseCase
import plus.vplan.app.ui.common.AttachedFile
import kotlin.uuid.ExperimentalUuidApi

class HomeworkDetailViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val toggleTaskDoneUseCase: ToggleTaskDoneUseCase,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val editHomeworkSubjectInstanceUseCase: EditHomeworkSubjectInstanceUseCase,
    private val editHomeworkDueToUseCase: EditHomeworkDueToUseCase,
    private val editHomeworkVisibilityUseCase: EditHomeworkVisibilityUseCase,
    private val deleteHomeworkUseCase: DeleteHomeworkUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val renameFileUseCase: RenameFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val addFileUseCase: AddFileUseCase
) : ViewModel() {
    var state by mutableStateOf(HomeworkDetailState())
        private set

    private var mainJob: Job? = null

    fun init(homeworkId: Int) {
        state = HomeworkDetailState()
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
                    canEdit = (homework is Homework.CloudHomework && homework.createdBy == profile.vppIdId) || (homework is Homework.LocalHomework && homework.createdByProfile == profile.id),
                    initDone = true
                )
            }.filterNotNull().collectLatest { state = it }
        }
    }

    fun onEvent(event: HomeworkDetailEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeworkDetailEvent.ToggleTaskDone -> toggleTaskDoneUseCase(event.task, state.profile!!)
                is HomeworkDetailEvent.UpdateSubjectInstance -> editHomeworkSubjectInstanceUseCase(state.homework!!, event.subjectInstance, state.profile!!)
                is HomeworkDetailEvent.UpdateDueTo -> editHomeworkDueToUseCase(state.homework!!, event.dueTo, state.profile!!)
                is HomeworkDetailEvent.UpdateVisibility -> editHomeworkVisibilityUseCase(state.homework as Homework.CloudHomework, event.isPublic, state.profile!!)
                is HomeworkDetailEvent.Reload -> {
                    state = state.copy(reloadingState = UnoptimisticTaskState.InProgress)
                    val result = updateHomeworkUseCase(state.homework!!.id)
                    when (result) {
                        UpdateResult.SUCCESS -> {
                            state = state.copy(reloadingState = UnoptimisticTaskState.Success)
                            viewModelScope.launch {
                                delay(2000)
                                if (state.reloadingState == UnoptimisticTaskState.Success) state = state.copy(reloadingState = null)
                            }
                        }
                        UpdateResult.ERROR -> state = state.copy(reloadingState = UnoptimisticTaskState.Error)
                        UpdateResult.DOES_NOT_EXIST -> state = state.copy(reloadingState = UnoptimisticTaskState.Success, deleteState = UnoptimisticTaskState.Success)
                    }
                }
                is HomeworkDetailEvent.DeleteHomework -> {
                    state = state.copy(deleteState = UnoptimisticTaskState.InProgress)
                    val result = deleteHomeworkUseCase(state.homework!!, state.profile!!)
                    state = if (result) {
                        state.copy(deleteState = UnoptimisticTaskState.Success)
                    } else {
                        state.copy(deleteState = UnoptimisticTaskState.Error)
                    }
                }
                is HomeworkDetailEvent.AddTask -> {
                    state = state.copy(newTaskState = UnoptimisticTaskState.InProgress)
                    val result = addTaskUseCase(state.homework!!, event.task, state.profile!!)
                    state = if (result) state.copy(newTaskState = UnoptimisticTaskState.Success) else state.copy(newTaskState = UnoptimisticTaskState.Error)
                }
                is HomeworkDetailEvent.UpdateTask -> updateTaskUseCase(event.task, event.newContent, state.profile!!)
                is HomeworkDetailEvent.DeleteTask -> {
                    if (state.homework!!.taskIds.size == 1) return@launch onEvent(HomeworkDetailEvent.DeleteHomework)
                    state = state.copy(taskDeleteState = state.taskDeleteState.plus(event.task.id to UnoptimisticTaskState.InProgress))
                    val result = deleteTaskUseCase(event.task, state.profile!!)
                    state = if (result) {
                        state.copy(taskDeleteState = state.taskDeleteState.plus(event.task.id to UnoptimisticTaskState.Success))
                    } else {
                        state.copy(taskDeleteState = state.taskDeleteState.plus(event.task.id to UnoptimisticTaskState.Error))
                    }
                }
                is HomeworkDetailEvent.DownloadFile -> {
                    downloadFileUseCase(event.file, state.profile!!).collectLatest {
                        state = state.copy(fileDownloadState = state.fileDownloadState.plus(event.file.id to it))
                    }
                    state = state.copy(fileDownloadState = state.fileDownloadState - event.file.id)
                }
                is HomeworkDetailEvent.RenameFile -> renameFileUseCase(event.file, event.newName, state.profile!!)
                is HomeworkDetailEvent.DeleteFile -> deleteFileUseCase(event.file, state.homework!!, state.profile!!)
                is HomeworkDetailEvent.AddFile -> addFileUseCase(state.homework!!, event.file.platformFile, state.profile!!)
            }
        }
    }
}

data class HomeworkDetailState(
    val homework: Homework? = null,
    val profile: Profile.StudentProfile? = null,
    val canEdit: Boolean = false,
    val reloadingState: UnoptimisticTaskState? = null,
    val deleteState: UnoptimisticTaskState? = null,
    val initDone: Boolean = false,
    val newTaskState: UnoptimisticTaskState? = null,
    val taskDeleteState: Map<Int, UnoptimisticTaskState> = emptyMap(),
    val fileDownloadState: Map<Int, Float> = emptyMap(),
)

sealed class HomeworkDetailEvent {
    data class ToggleTaskDone(val task: Homework.HomeworkTask) : HomeworkDetailEvent()
    data class UpdateSubjectInstance(val subjectInstance: SubjectInstance?) : HomeworkDetailEvent()
    data class UpdateDueTo(val dueTo: LocalDate) : HomeworkDetailEvent()
    data class UpdateVisibility(val isPublic: Boolean) : HomeworkDetailEvent()
    data class AddTask(val task: String) : HomeworkDetailEvent()
    data class UpdateTask(val task: Homework.HomeworkTask, val newContent: String) : HomeworkDetailEvent()
    data class DeleteTask(val task: Homework.HomeworkTask) : HomeworkDetailEvent()
    data object DeleteHomework : HomeworkDetailEvent()
    data class DownloadFile(val file: File) : HomeworkDetailEvent()

    data class RenameFile(val file: File, val newName: String) : HomeworkDetailEvent()
    data class DeleteFile(val file: File) : HomeworkDetailEvent()
    data class AddFile(val file: AttachedFile) : HomeworkDetailEvent()
    data object Reload : HomeworkDetailEvent()
}

private suspend fun Profile.StudentProfile.prefetch() {
    this.getGroupItem()
    this.getSubjectInstances().onEach {
        it.getCourseItem()
        it.getTeacherItem()
    }
}

private suspend fun Homework.prefetch() {
    this.subjectInstance?.getFirstValue()
    this.getFileItems()
    if (this is Homework.CloudHomework) this.getCreatedBy()
}

enum class UnoptimisticTaskState {
    InProgress, Error, Success
}