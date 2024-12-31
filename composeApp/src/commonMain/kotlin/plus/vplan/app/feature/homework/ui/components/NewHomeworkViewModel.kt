package plus.vplan.app.feature.homework.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.extension
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.homework.domain.usecase.HideVppIdBannerUseCase
import plus.vplan.app.feature.homework.domain.usecase.IsVppIdBannerAllowedUseCase
import plus.vplan.app.utils.getBitmapFromBytes
import plus.vplan.app.utils.getBitmapFromPdf
import kotlin.uuid.Uuid

class NewHomeworkViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val isVppIdBannerAllowedUseCase: IsVppIdBannerAllowedUseCase,
    private val hideVppIdBannerUseCase: HideVppIdBannerUseCase
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
                    currentProfile = currentProfile as? Profile.StudentProfile,
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
                is NewHomeworkEvent.SelectDefaultLesson -> state = state.copy(selectedDefaultLesson = event.defaultLesson)
                is NewHomeworkEvent.SelectDate -> state = state.copy(selectedDate = event.date)
                is NewHomeworkEvent.SetVisibility -> state = state.copy(isPublic = event.isPublic)
                is NewHomeworkEvent.AddFile -> {
                    val bitmap = if (event.file.extension == "pdf") {
                        getBitmapFromPdf(event.file.readBytes())
                    } else {
                        getBitmapFromBytes(event.file.readBytes())
                    }
                    state = state.copy(files = state.files + Document(event.file, bitmap))
                }
                is NewHomeworkEvent.HideVppIdBanner -> hideVppIdBannerUseCase()
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
    val files: List<Document> = emptyList(),
    val canShowVppIdBanner: Boolean = false
)

data class Document(
    val platformFile: PlatformFile,
    val bitmap: ImageBitmap?
)

sealed class NewHomeworkEvent {
    data class AddTask(val task: String) : NewHomeworkEvent()
    data class UpdateTask(val taskId: Uuid, val task: String) : NewHomeworkEvent()
    data class RemoveTask(val taskId: Uuid) : NewHomeworkEvent()

    data class SelectDefaultLesson(val defaultLesson: DefaultLesson?) : NewHomeworkEvent()
    data class SelectDate(val date: LocalDate) : NewHomeworkEvent()

    data class SetVisibility(val isPublic: Boolean) : NewHomeworkEvent()

    data class AddFile(val file: PlatformFile) : NewHomeworkEvent()

    data object HideVppIdBanner : NewHomeworkEvent()
}