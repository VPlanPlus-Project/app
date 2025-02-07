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
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.homework.domain.usecase.CreateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.HideVppIdBannerUseCase
import plus.vplan.app.feature.homework.domain.usecase.IsVppIdBannerAllowedUseCase
import plus.vplan.app.ui.common.AttachedFile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class NewHomeworkViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val isVppIdBannerAllowedUseCase: IsVppIdBannerAllowedUseCase,
    private val hideVppIdBannerUseCase: HideVppIdBannerUseCase,
    private val createHomeworkUseCase: CreateHomeworkUseCase
) : ViewModel() {
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
                        it?.getDefaultLessons()?.onEach { defaultLesson ->
                            defaultLesson.getTeacherItem()
                            defaultLesson.getCourseItem()
                        }
                    },
                    isPublic = if ((currentProfile as? Profile.StudentProfile)?.vppId == null) null else true,
                    canShowVppIdBanner = canShowVppIdBanner
                )
            }.collect { state = it }
        }
    }

    fun onEvent(event: NewHomeworkEvent) {
        viewModelScope.launch {
            when (event) {
                is NewHomeworkEvent.AddTask -> state = state.copy(tasks = state.tasks.plus(Uuid.random() to event.task))
                is NewHomeworkEvent.UpdateTask -> state = state.copy(tasks = state.tasks.plus(event.taskId to event.task))
                is NewHomeworkEvent.RemoveTask -> state = state.copy(tasks = state.tasks.minus(event.taskId))
                is NewHomeworkEvent.SelectDefaultLesson -> state = state.copy(selectedDefaultLesson = event.defaultLesson.also {
                    it?.getCourseItem()
                    it?.getTeacherItem()
                    it?.getGroupItems()
                })
                is NewHomeworkEvent.SelectDate -> state = state.copy(selectedDate = event.date)
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
                    if (state.tasks.isEmpty()) return@launch
                    if (state.currentProfile == null) return@launch
                    if (state.selectedDate == null) return@launch
                    createHomeworkUseCase(state.tasks.values.toList(), state.isPublic, state.selectedDate!!, state.selectedDefaultLesson, state.files)
                }
            }
        }
    }
}

data class NewHomeworkState(
    val tasks: Map<Uuid, String> = emptyMap(),
    val currentProfile: Profile.StudentProfile? = null,
    val selectedDefaultLesson: DefaultLesson? = null,
    val selectedDate: LocalDate? = null,
    val isPublic: Boolean? = null,
    val files: List<AttachedFile> = emptyList(),
    val canShowVppIdBanner: Boolean = false
)

sealed class NewHomeworkEvent {
    data class AddTask(val task: String) : NewHomeworkEvent()
    data class UpdateTask(val taskId: Uuid, val task: String) : NewHomeworkEvent()
    data class RemoveTask(val taskId: Uuid) : NewHomeworkEvent()

    data class SelectDefaultLesson(val defaultLesson: DefaultLesson?) : NewHomeworkEvent()
    data class SelectDate(val date: LocalDate) : NewHomeworkEvent()

    data class SetVisibility(val isPublic: Boolean) : NewHomeworkEvent()

    data class AddFile(val file: PlatformFile) : NewHomeworkEvent()
    data class UpdateFile(val file: AttachedFile) : NewHomeworkEvent()
    data class RemoveFile(val file: AttachedFile) : NewHomeworkEvent()

    data object HideVppIdBanner : NewHomeworkEvent()
    data object Save : NewHomeworkEvent()
}