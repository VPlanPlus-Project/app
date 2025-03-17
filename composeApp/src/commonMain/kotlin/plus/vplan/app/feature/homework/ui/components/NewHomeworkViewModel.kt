@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.homework.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
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
    private val createHomeworkUseCase: CreateHomeworkUseCase
) : ViewModel() {
    @OptIn(ExperimentalUuidApi::class)
    var state by mutableStateOf(NewHomeworkState())
        private set

    init {
        viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                isVppIdBannerAllowedUseCase()
            ) { currentProfile, canShowVppIdBanner ->
                state.copy(
                    currentProfile = (currentProfile as? Profile.StudentProfile).also {
                        it?.getGroupItem()
                        it?.getSubjectInstances()?.onEach { subjectInstance ->
                            subjectInstance.getTeacherItem()
                            subjectInstance.getCourseItem()
                        }
                    },
                    isPublic = if ((currentProfile as? Profile.StudentProfile)?.vppIdId == null) null else true,
                    canShowVppIdBanner = canShowVppIdBanner
                )
            }.collect { state = it }
        }
    }

    fun onEvent(event: NewHomeworkEvent) {
        viewModelScope.launch {
            when (event) {
                is NewHomeworkEvent.AddTask -> state = state.copy(tasks = state.tasks.plus(Uuid.random() to event.task), showTasksError = state.showTasksError && state.tasks.all { it.value.isBlank() })
                is NewHomeworkEvent.UpdateTask -> state = state.copy(tasks = state.tasks.plus(event.taskId to event.task), showTasksError = state.showTasksError && state.tasks.all { it.value.isBlank() })
                is NewHomeworkEvent.RemoveTask -> state = state.copy(tasks = state.tasks.minus(event.taskId))
                is NewHomeworkEvent.SelectSubjectInstance -> state = state.copy(selectedSubjectInstance = event.subjectInstance.also {
                    it?.getCourseItem()
                    it?.getTeacherItem()
                    it?.getGroupItems()
                })
                is NewHomeworkEvent.SelectDate -> state = state.copy(selectedDate = event.date, showDateError = false)
                is NewHomeworkEvent.SetVisibility -> state = state.copy(isPublic = event.isPublic)
                is NewHomeworkEvent.AddFile -> {
                    val file = AttachedFile.fromFile(event.file)
                    state = state.copy(files = state.files + file)
                }
                is NewHomeworkEvent.UpdateFile -> {
                    state = state.copy(files = state.files.map { file -> if (file.platformFile.path.hashCode() == event.file.platformFile.path.hashCode()) event.file else file })
                }
                is NewHomeworkEvent.RemoveFile -> {
                    state = state.copy(files = state.files.filter { it.platformFile.path.hashCode() != event.file.platformFile.path.hashCode() })
                }
                is NewHomeworkEvent.HideVppIdBanner -> hideVppIdBannerUseCase()
                is NewHomeworkEvent.Save -> {
                    if (state.savingState == UnoptimisticTaskState.InProgress) return@launch
                    state = state.copy(savingState = UnoptimisticTaskState.InProgress)
                    if (state.currentProfile == null) return@launch
                    if (state.tasks.all { it.value.isBlank() }) state = state.copy(showTasksError = true)
                    if (state.selectedDate == null) state = state.copy(showDateError = true)
                    run save@{
                        return@save createHomeworkUseCase(
                            tasks = state.tasks.values.mapNotNull { it.ifBlank { null } }.toList().ifEmpty { return@save false },
                            isPublic = state.isPublic,
                            date = state.selectedDate ?: return@save false,
                            subjectInstance = state.selectedSubjectInstance,
                            selectedFiles = state.files
                        )
                    }.let { state = state.copy(savingState = if (it) UnoptimisticTaskState.Success else UnoptimisticTaskState.Error) }
                }
            }
        }
    }
}

data class NewHomeworkState(
    val tasks: Map<Uuid, String> = emptyMap(),
    val currentProfile: Profile.StudentProfile? = null,
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