@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.feature.homework.ui.components.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.file.FileOperationProgress
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.domain.usecase.file.DeleteFileUseCase
import plus.vplan.app.domain.usecase.file.DownloadFileUseCase
import plus.vplan.app.domain.usecase.file.GetFileThumbnailUseCase
import plus.vplan.app.domain.usecase.file.OpenFileUseCase
import plus.vplan.app.domain.usecase.file.RenameFileUseCase
import plus.vplan.app.domain.usecase.file.UploadFileUseCase
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult
import plus.vplan.app.feature.homework.domain.usecase.AddTaskUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteTaskUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkDueToUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkSubjectInstanceUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkVisibilityUseCase
import plus.vplan.app.feature.homework.domain.usecase.ToggleTaskDoneUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateTaskUseCase
import plus.vplan.app.ui.common.AttachedFile

class HomeworkDetailViewModel(
    private val homeworkRepository: HomeworkRepository,
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
    private val uploadFileUseCase: UploadFileUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val openFileUseCase: OpenFileUseCase,
    private val renameFileUseCase: RenameFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    val getFileThumbnailUseCase: GetFileThumbnailUseCase,
    private val keyValueRepository: KeyValueRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
) : ViewModel() {
    val state: StateFlow<HomeworkDetailState>
        field = MutableStateFlow(HomeworkDetailState())

    private var mainJob: Job? = null

    init {
        viewModelScope.launch {
            keyValueRepository.get(Keys.DEVELOPER_SETTINGS_ACTIVE).collectLatest { value ->
                val isDeveloperMode = value == "true"
                state.update { state -> state.copy(isDeveloperMode = isDeveloperMode) }
            }
        }
    }

    fun init(homeworkId: Int) {
        state.update { state -> state.copy(
            homework = null,
            profile = null,
            subjectInstances = emptyList(),
            canEdit = false
        ) }
        mainJob?.cancel()
        mainJob = viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                homeworkRepository.getById(homeworkId).filterNotNull()
            ) { profile, homework ->
                if (profile !is Profile.StudentProfile) return@combine null

                state.update { state ->
                    state.copy(
                        homework = homework,
                        profile = profile,
                        canEdit = (homework.creator is AppEntity.VppId && (homework.creator as AppEntity.VppId).vppId.id == profile.vppId?.id) || (homework.creator is AppEntity.Profile && (homework.creator as AppEntity.Profile).profile.id == profile.id),
                        initDone = true
                    )
                }

                coroutineScope {
                    subjectInstanceRepository
                        .getByGroup(profile.group)
                        .map { subjectInstances ->
                            subjectInstances.filter { subjectInstance ->
                                profile.subjectInstanceConfiguration.toList().firstOrNull { it.first.id == subjectInstance.id }?.second != false
                            }
                        }
                        .map { subjectInstances -> subjectInstances.sortedBy { it.subject } }
                        .onEach { state.update { state -> state.copy(subjectInstances = it) } }
                        .launchIn(this)
                }

            }.collect()
        }
    }

    fun onEvent(event: HomeworkDetailEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeworkDetailEvent.ToggleTaskDone -> {
                    val currentHomework = state.value.homework ?: return@launch
                    val profile = state.value.profile ?: return@launch
                    val oldTask = event.task
                    val wasDone = oldTask.isDone(profile)
                    
                    // Optimistic update - immediately toggle in UI
                    val updatedTasks = currentHomework.tasks.map { task ->
                        if (task.id == oldTask.id) {
                            val newDoneByProfiles = if (wasDone) {
                                task.doneByProfiles - profile.id
                            } else {
                                task.doneByProfiles + profile.id
                            }
                            val newDoneByVppIds = if (profile.vppId != null) {
                                if (wasDone) {
                                    task.doneByVppIds - profile.vppId!!.id
                                } else {
                                    task.doneByVppIds + profile.vppId!!.id
                                }
                            } else {
                                task.doneByVppIds
                            }
                            task.copy(doneByProfiles = newDoneByProfiles, doneByVppIds = newDoneByVppIds)
                        } else task
                    }
                    
                    // Update state optimistically
                    val updatedHomework = when (currentHomework) {
                        is Homework.CloudHomework -> currentHomework.copy(tasks = updatedTasks)
                        is Homework.LocalHomework -> currentHomework.copy(tasks = updatedTasks)
                    }
                    state.update { it.copy(homework = updatedHomework) }
                    
                    // Perform actual toggle
                    val success = toggleTaskDoneUseCase(event.task, profile)
                    
                    // If failed, revert the optimistic update
                    if (!success) {
                        val revertedHomework = when (currentHomework) {
                            is Homework.CloudHomework -> currentHomework.copy(tasks = updatedTasks)
                            is Homework.LocalHomework -> currentHomework.copy(tasks = updatedTasks)
                        }
                        state.update { it.copy(homework = revertedHomework) }
                    }
                }
                is HomeworkDetailEvent.UpdateSubjectInstance -> editHomeworkSubjectInstanceUseCase(state.value.homework!!, event.subjectInstance, state.value.profile!!)
                is HomeworkDetailEvent.UpdateDueTo -> editHomeworkDueToUseCase(state.value.homework!!, event.dueTo, state.value.profile!!)
                is HomeworkDetailEvent.UpdateVisibility -> editHomeworkVisibilityUseCase(state.value.homework as Homework.CloudHomework, event.isPublic, state.value.profile!!)
                is HomeworkDetailEvent.Reload -> {
                    state.update { state -> state.copy(reloadingState = UnoptimisticTaskState.InProgress) }
                    val result = updateHomeworkUseCase(state.value.homework!!.id)
                    when (result) {
                        UpdateResult.SUCCESS -> {
                            state.update { state -> state.copy(reloadingState = UnoptimisticTaskState.Success) }
                            viewModelScope.launch {
                                delay(2000)
                                if (state.value.reloadingState == UnoptimisticTaskState.Success) {
                                    state.update { state -> state.copy(reloadingState = null) }
                                }
                            }
                        }
                        UpdateResult.ERROR -> state.update { state -> state.copy(reloadingState = UnoptimisticTaskState.Error) }
                        UpdateResult.DOES_NOT_EXIST -> state.update { state -> state.copy(reloadingState = UnoptimisticTaskState.Success, deleteState = UnoptimisticTaskState.Success) }
                    }
                }
                is HomeworkDetailEvent.DeleteHomework -> {
                    state.update { state -> state.copy(deleteState = UnoptimisticTaskState.InProgress) }
                    val result = deleteHomeworkUseCase(state.value.homework!!, state.value.profile!!)
                    state.update { state ->
                        state.copy(
                            deleteState = if (result) UnoptimisticTaskState.Success else UnoptimisticTaskState.Error
                        )
                    }
                }
                is HomeworkDetailEvent.AddTask -> {
                    state.update { state -> state.copy(newTaskState = UnoptimisticTaskState.InProgress) }
                    val result = addTaskUseCase(state.value.homework!!, event.task, state.value.profile!!)
                    state.update { state ->
                        state.copy(
                            newTaskState = if (result) UnoptimisticTaskState.Success else UnoptimisticTaskState.Error
                        )
                    }
                }
                is HomeworkDetailEvent.UpdateTask -> updateTaskUseCase(event.task, event.newContent, state.value.profile!!)
                is HomeworkDetailEvent.DeleteTask -> {
                    if (state.value.homework!!.tasks.size == 1) return@launch onEvent(HomeworkDetailEvent.DeleteHomework)
                    state.update { state -> state.copy(taskDeleteState = state.taskDeleteState.plus(event.task.id to UnoptimisticTaskState.InProgress)) }
                    val result = deleteTaskUseCase(event.task, state.value.profile!!)
                    state.update { state ->
                        state.copy(
                            taskDeleteState = state.taskDeleteState.plus(event.task.id to if (result) UnoptimisticTaskState.Success else UnoptimisticTaskState.Error)
                        )
                    }
                }
                is HomeworkDetailEvent.DownloadFile -> {
                    val profile = state.value.profile ?: return@launch
                    val schoolAuth = profile.vppId?.buildVppSchoolAuthentication(-1) ?: profile.school.buildSp24AppAuthentication()
                    downloadFileUseCase(event.file, schoolAuth).collectLatest { progress ->
                        state.update { state ->
                            state.copy(
                                fileOperationState = state.fileOperationState.plus(event.file.id to progress)
                            )
                        }
                    }
                }
                is HomeworkDetailEvent.OpenFile -> {
                    openFileUseCase(event.file)
                }
                is HomeworkDetailEvent.RenameFile -> {
                    renameFileUseCase(event.file, event.newName, state.value.profile?.vppId)
                }
                is HomeworkDetailEvent.DeleteFile -> {
                    deleteFileUseCase(event.file, state.value.profile?.vppId)
                }
                is HomeworkDetailEvent.AddFile -> {
                    val profile = state.value.profile ?: return@launch
                    val homework = state.value.homework ?: return@launch
                    val vppId = profile.vppId ?: return@launch // Require VPP ID for upload
                    
                    when (val uploadResult = uploadFileUseCase(vppId, event.file.platformFile)) {
                        is Response.Success -> {
                            val uploadedFile = uploadResult.data
                            // Link the file to the homework
                            if (homework.id > 0) {
                                homeworkRepository.linkHomeworkFile(
                                    vppId = vppId,
                                    homeworkId = homework.id,
                                    fileId = uploadedFile.id
                                )
                            }
                        }
                        is Response.Error, is Response.Loading -> {
                            // Handle error or loading state
                        }
                    }
                }
            }
        }
    }
}

data class HomeworkDetailState(
    val homework: Homework? = null,

    val profile: Profile.StudentProfile? = null,
    val subjectInstances: List<SubjectInstance> = emptyList(),
    val canEdit: Boolean = false,
    val reloadingState: UnoptimisticTaskState? = null,
    val deleteState: UnoptimisticTaskState? = null,
    val initDone: Boolean = false,
    val newTaskState: UnoptimisticTaskState? = null,
    val taskDeleteState: Map<Int, UnoptimisticTaskState> = emptyMap(),
    val fileOperationState: Map<Int, FileOperationProgress> = emptyMap(),
    val isDeveloperMode: Boolean = false
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
    data class OpenFile(val file: File) : HomeworkDetailEvent()
    data class RenameFile(val file: File, val newName: String) : HomeworkDetailEvent()
    data class DeleteFile(val file: File) : HomeworkDetailEvent()
    data class AddFile(val file: AttachedFile) : HomeworkDetailEvent()
    data object Reload : HomeworkDetailEvent()
}

enum class UnoptimisticTaskState {
    InProgress, Error, Success
}