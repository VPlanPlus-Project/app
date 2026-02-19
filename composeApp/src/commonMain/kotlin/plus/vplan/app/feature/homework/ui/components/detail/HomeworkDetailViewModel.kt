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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.model.File
import plus.vplan.app.core.model.Homework
import plus.vplan.app.domain.model.populated.HomeworkPopulator
import plus.vplan.app.domain.model.populated.PopulatedHomework
import plus.vplan.app.domain.model.populated.PopulatedSubjectInstance
import plus.vplan.app.domain.model.populated.PopulationContext
import plus.vplan.app.domain.model.populated.SubjectInstancePopulator
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult
import plus.vplan.app.feature.homework.domain.usecase.AddFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.AddTaskUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteTaskUseCase
import plus.vplan.app.feature.homework.domain.usecase.DownloadFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkDueToUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkSubjectInstanceUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkVisibilityUseCase
import plus.vplan.app.feature.homework.domain.usecase.RenameFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.ToggleTaskDoneUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateTaskUseCase
import plus.vplan.app.ui.common.AttachedFile

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
    private val addFileUseCase: AddFileUseCase,
    private val keyValueRepository: KeyValueRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val subjectInstancePopulator: SubjectInstancePopulator,
    private val homeworkPopulator: HomeworkPopulator,
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
            homeworkSubjectInstance = null,
            profile = null,
            subjectInstances = emptyList(),
            canEdit = false
        ) }
        mainJob?.cancel()
        mainJob = viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                App.homeworkSource.getById(homeworkId).filterIsInstance<CacheState.Done<Homework>>().flatMapLatest {
                    homeworkPopulator.populateSingle(it.data)
                }
            ) { profile, homework ->
                if (profile !is Profile.StudentProfile) return@combine null

                state.update { state ->
                    state.copy(
                        homework = homework,
                        profile = profile,
                        canEdit = (homework is PopulatedHomework.CloudHomework && homework.createdByUser.id == profile.vppId?.id) || (homework is PopulatedHomework.LocalHomework && homework.createdByProfile.id == profile.id),
                        initDone = true
                    )
                }

                coroutineScope {
                    homework.subjectInstance?.id.let evaluateSubjectInstance@{ subjectInstanceId ->
                        if (subjectInstanceId == null) {
                            state.update { state -> state.copy(homeworkSubjectInstance = null) }
                            return@evaluateSubjectInstance
                        }

                        subjectInstanceRepository
                            .getByLocalId(subjectInstanceId)
                            .filterNotNull()
                            .flatMapLatest { subjectInstance -> subjectInstancePopulator.populateSingle(subjectInstance) }
                            .onEach { state.update { state -> state.copy(homeworkSubjectInstance = it) } }
                            .launchIn(this)
                    }

                    subjectInstanceRepository
                        .getByGroup(profile.group.id)
                        .map { subjectInstances ->
                            subjectInstances.filter { subjectInstance ->
                                profile.subjectInstanceConfiguration[subjectInstance.id] != false
                            }
                        }
                        .map { subjectInstances -> subjectInstances.sortedBy { it.subject } }
                        .flatMapLatest { subjectInstancePopulator.populateMultiple(it, PopulationContext.Profile(state.value.profile!!)) }
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
                        is PopulatedHomework.CloudHomework -> currentHomework.copy(tasks = updatedTasks)
                        is PopulatedHomework.LocalHomework -> currentHomework.copy(tasks = updatedTasks)
                    }
                    state.update { it.copy(homework = updatedHomework) }
                    
                    // Perform actual toggle
                    val success = toggleTaskDoneUseCase(event.task, profile)
                    
                    // If failed, revert the optimistic update
                    if (!success) {
                        val revertedHomework = when (currentHomework) {
                            is PopulatedHomework.CloudHomework -> currentHomework.copy(tasks = currentHomework.tasks)
                            is PopulatedHomework.LocalHomework -> currentHomework.copy(tasks = currentHomework.tasks)
                        }
                        state.update { it.copy(homework = revertedHomework) }
                    }
                }
                is HomeworkDetailEvent.UpdateSubjectInstance -> editHomeworkSubjectInstanceUseCase(state.value.homework!!, event.subjectInstance?.subjectInstance, state.value.profile!!)
                is HomeworkDetailEvent.UpdateDueTo -> editHomeworkDueToUseCase(state.value.homework!!.homework, event.dueTo, state.value.profile!!)
                is HomeworkDetailEvent.UpdateVisibility -> editHomeworkVisibilityUseCase(state.value.homework?.homework as Homework.CloudHomework, event.isPublic, state.value.profile!!)
                is HomeworkDetailEvent.Reload -> {
                    state.update { state -> state.copy(reloadingState = UnoptimisticTaskState.InProgress) }
                    val result = updateHomeworkUseCase(state.value.homework!!.homework.id)
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
                    val result = deleteHomeworkUseCase(state.value.homework!!.homework, state.value.profile!!)
                    state.update { state ->
                        state.copy(
                            deleteState = if (result) UnoptimisticTaskState.Success else UnoptimisticTaskState.Error
                        )
                    }
                }
                is HomeworkDetailEvent.AddTask -> {
                    state.update { state -> state.copy(newTaskState = UnoptimisticTaskState.InProgress) }
                    val result = addTaskUseCase(state.value.homework!!.homework, event.task, state.value.profile!!)
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
                    downloadFileUseCase(event.file, state.value.profile!!).collectLatest {
                        state.update { state ->
                            state.copy(
                                fileDownloadState = state.fileDownloadState.plus(event.file.id to it)
                            )
                        }
                    }
                }
                is HomeworkDetailEvent.RenameFile -> renameFileUseCase(event.file, event.newName, state.value.profile!!)
                is HomeworkDetailEvent.DeleteFile -> deleteFileUseCase(event.file, state.value.homework!!.homework, state.value.profile!!)
                is HomeworkDetailEvent.AddFile -> addFileUseCase(state.value.homework!!.homework, event.file.platformFile, state.value.profile!!)
            }
        }
    }
}

data class HomeworkDetailState(
    val homework: PopulatedHomework? = null,
    val homeworkSubjectInstance: PopulatedSubjectInstance? = null,

    val profile: Profile.StudentProfile? = null,
    val subjectInstances: List<PopulatedSubjectInstance> = emptyList(),
    val canEdit: Boolean = false,
    val reloadingState: UnoptimisticTaskState? = null,
    val deleteState: UnoptimisticTaskState? = null,
    val initDone: Boolean = false,
    val newTaskState: UnoptimisticTaskState? = null,
    val taskDeleteState: Map<Int, UnoptimisticTaskState> = emptyMap(),
    val fileDownloadState: Map<Int, Float> = emptyMap(),
    val isDeveloperMode: Boolean = false
)

sealed class HomeworkDetailEvent {
    data class ToggleTaskDone(val task: Homework.HomeworkTask) : HomeworkDetailEvent()
    data class UpdateSubjectInstance(val subjectInstance: PopulatedSubjectInstance?) : HomeworkDetailEvent()
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

enum class UnoptimisticTaskState {
    InProgress, Error, Success
}