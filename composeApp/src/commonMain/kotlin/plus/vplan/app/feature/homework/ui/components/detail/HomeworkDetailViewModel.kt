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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.core.model.SubjectInstance
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
                App.homeworkSource.getById(homeworkId)
            ) { profile, homeworkData ->
                if (homeworkData !is CacheState.Done || profile !is Profile.StudentProfile) return@combine null
                val homework = homeworkData.data

                homework.prefetch()

                state.update { state ->
                    state.copy(
                        homework = homework,
                        profile = profile,
                        canEdit = (homework is Homework.CloudHomework && homework.createdById == profile.vppId?.id) || (homework is Homework.LocalHomework && homework.createdByProfileId == profile.id),
                        initDone = true
                    )
                }

                coroutineScope {
                    homework.subjectInstanceId.let evaluateSubjectInstance@{ subjectInstanceId ->
                        if (subjectInstanceId == null) {
                            state.update { state -> state.copy(homeworkSubjectInstance = null) }
                            return@evaluateSubjectInstance
                        }

                        subjectInstanceRepository
                            .findByAlias(Alias(AliasProvider.Vpp, subjectInstanceId.toString(), 1), false, true)
                            .filterIsInstance<AliasState.Done<SubjectInstance>>().map { it.data }
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
                is HomeworkDetailEvent.ToggleTaskDone -> toggleTaskDoneUseCase(event.task, state.value.profile!!)
                is HomeworkDetailEvent.UpdateSubjectInstance -> editHomeworkSubjectInstanceUseCase(state.value.homework!!, event.subjectInstance?.subjectInstance, state.value.profile!!)
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
                    if (state.value.homework!!.taskIds.size == 1) return@launch onEvent(HomeworkDetailEvent.DeleteHomework)
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
                is HomeworkDetailEvent.DeleteFile -> deleteFileUseCase(event.file, state.value.homework!!, state.value.profile!!)
                is HomeworkDetailEvent.AddFile -> addFileUseCase(state.value.homework!!, event.file.platformFile, state.value.profile!!)
            }
        }
    }
}

data class HomeworkDetailState(
    val homework: Homework? = null,
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

private suspend fun Homework.prefetch() {
    this.subjectInstance?.getFirstValue()
    this.getFileItems()
}

enum class UnoptimisticTaskState {
    InProgress, Error, Success
}