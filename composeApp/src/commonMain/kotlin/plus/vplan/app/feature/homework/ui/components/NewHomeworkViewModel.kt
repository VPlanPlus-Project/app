@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.homework.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.populated.PopulatedSubjectInstance
import plus.vplan.app.domain.model.populated.PopulationContext
import plus.vplan.app.domain.model.populated.SubjectInstancePopulator
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.homework.domain.usecase.CreateHomeworkResult
import plus.vplan.app.feature.homework.domain.usecase.CreateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.HideVppIdBannerUseCase
import plus.vplan.app.feature.homework.domain.usecase.IsVppIdBannerAllowedUseCase
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState
import plus.vplan.app.ui.common.AttachedFile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class NewHomeworkViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val isVppIdBannerAllowedUseCase: IsVppIdBannerAllowedUseCase,
    private val hideVppIdBannerUseCase: HideVppIdBannerUseCase,
    private val createHomeworkUseCase: CreateHomeworkUseCase,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val subjectInstancePopulator: SubjectInstancePopulator,
) : ViewModel() {
    @OptIn(ExperimentalUuidApi::class)
    val state = MutableStateFlow(NewHomeworkState())

    /**
     * Holds a reference to the currently active coroutine job responsible for loading user profile and configuration data.
     * This ensures that all necessary information is available before a new homework entry is created. This is automatically
     * reset when [init] is called to prevent multiple concurrent data loading operations.
     */
    private var dataJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun init() {
        state.value = NewHomeworkState()
        dataJob = viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                isVppIdBannerAllowedUseCase()
            ) { currentProfile, canShowVppIdBanner ->
                state.update { state ->
                    state.copy(
                        currentProfile = (currentProfile as? Profile.StudentProfile),
                        isPublic = if ((currentProfile as? Profile.StudentProfile)?.vppId?.id == null) null else true,
                        canShowVppIdBanner = canShowVppIdBanner
                    )
                }
            }.collectLatest {
                val profile = state.value.currentProfile ?: return@collectLatest

                subjectInstanceRepository
                    .getByGroup(profile.group.id)
                    .map { subjectInstances ->
                        subjectInstances.filter { subjectInstance ->
                            profile.subjectInstanceConfiguration[subjectInstance.id] != false
                        }
                    }
                    .map { subjectInstances -> subjectInstances.sortedBy { it.subject } }
                    .flatMapLatest { subjectInstancePopulator.populateMultiple(it, PopulationContext.Profile(profile)) }
                    .collectLatest {
                        state.update { state -> state.copy(subjectInstances = it) }
                    }
            }
        }
    }

    fun onEvent(event: NewHomeworkEvent) {
        viewModelScope.launch {
            when (event) {
                is NewHomeworkEvent.AddTask -> state.update { state -> state.copy(tasks = state.tasks.plus(Uuid.random() to event.task), showTasksError = state.showTasksError && state.tasks.all { it.value.isBlank() }) }
                is NewHomeworkEvent.UpdateTask -> state.update { state -> state.copy(tasks = state.tasks.plus(event.taskId to event.task), showTasksError = state.showTasksError && state.tasks.all { it.value.isBlank() }) }
                is NewHomeworkEvent.RemoveTask -> state.update { state -> state.copy(tasks = state.tasks.minus(event.taskId)) }
                is NewHomeworkEvent.SelectSubjectInstance -> state.update { state -> state.copy(selectedSubjectInstance = event.subjectInstance.also {
                    it?.getTeacherItem()
                    it?.getGroupItems()
                }) }
                is NewHomeworkEvent.SelectDate -> state.update { state -> state.copy(selectedDate = event.date, showDateError = false) }
                is NewHomeworkEvent.SetVisibility -> state.update { state -> state.copy(isPublic = event.isPublic) }
                is NewHomeworkEvent.AddFile -> {
                    val file = AttachedFile.fromFile(event.file)
                    state.update { state -> state.copy(files = state.files + file) }
                }
                is NewHomeworkEvent.UpdateFile -> {
                    state.update { state -> state.copy(files = state.files.map { file -> if (file.platformFile.path.hashCode() == event.file.platformFile.path.hashCode()) event.file else file }) }
                }
                is NewHomeworkEvent.RemoveFile -> {
                    state.update { state -> state.copy(files = state.files.filter { it.platformFile.path.hashCode() != event.file.platformFile.path.hashCode() }) }
                }
                is NewHomeworkEvent.HideVppIdBanner -> hideVppIdBannerUseCase()
                is NewHomeworkEvent.Save -> {
                    val currentState = state.value
                    if (currentState.savingState == UnoptimisticTaskState.InProgress) return@launch
                    state.update { it.copy(savingState = UnoptimisticTaskState.InProgress) }
                    if (currentState.currentProfile == null) return@launch
                    if (currentState.tasks.all { it.value.isBlank() }) state.update { it.copy(showTasksError = true) }
                    if (currentState.selectedDate == null) state.update { it.copy(showDateError = true) }
                    val result = run save@{
                        return@save createHomeworkUseCase(
                            tasks = currentState.tasks.values.mapNotNull { it.ifBlank { null } }.toList().ifEmpty { return@save false },
                            isPublic = currentState.isPublic,
                            date = currentState.selectedDate ?: return@save false,
                            subjectInstance = currentState.selectedSubjectInstance,
                            selectedFiles = currentState.files
                        )
                    }
                    state.update { it.copy(savingState = if (result !is CreateHomeworkResult.Success) {
                        Logger.d { "Failed to save homework: $result" }
                        UnoptimisticTaskState.Error
                    } else {
                        UnoptimisticTaskState.Success
                    }) }
                }
            }
        }
    }
}

data class NewHomeworkState(
    val tasks: Map<Uuid, String> = emptyMap(),
    val currentProfile: Profile.StudentProfile? = null,
    val subjectInstances: List<PopulatedSubjectInstance> = emptyList(),
    val selectedSubjectInstance: SubjectInstance? = null,
    val selectedDate: LocalDate? = null,
    val isPublic: Boolean? = null,
    val files: List<AttachedFile> = emptyList(),
    val canShowVppIdBanner: Boolean = false,

    val showTasksError: Boolean = false,
    val showDateError: Boolean = false,

    val savingState: UnoptimisticTaskState? = null
) {
    val hasInputErrors = showTasksError || showDateError
    val hasChanges: Boolean = tasks.isNotEmpty() || selectedSubjectInstance != null || selectedDate != null || isPublic != null || files.isNotEmpty()
}

sealed class NewHomeworkEvent {
    data class AddTask(val task: String) : NewHomeworkEvent()
    data class UpdateTask(val taskId: Uuid, val task: String) : NewHomeworkEvent()
    data class RemoveTask(val taskId: Uuid) : NewHomeworkEvent()

    data class SelectSubjectInstance(val subjectInstance: SubjectInstance?) : NewHomeworkEvent()
    data class SelectDate(val date: LocalDate) : NewHomeworkEvent()

    data class SetVisibility(val isPublic: Boolean) : NewHomeworkEvent()

    data class AddFile(val file: PlatformFile) : NewHomeworkEvent()
    data class UpdateFile(val file: AttachedFile) : NewHomeworkEvent()
    data class RemoveFile(val file: AttachedFile) : NewHomeworkEvent()

    data object HideVppIdBanner : NewHomeworkEvent()
    data object Save : NewHomeworkEvent()
}