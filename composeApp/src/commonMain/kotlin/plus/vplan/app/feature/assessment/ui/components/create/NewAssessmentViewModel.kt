package plus.vplan.app.feature.assessment.ui.components.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.homework.domain.usecase.HideVppIdBannerUseCase
import plus.vplan.app.feature.homework.domain.usecase.IsVppIdBannerAllowedUseCase
import plus.vplan.app.ui.common.AttachedFile

class NewAssessmentViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val isVppIdBannerAllowedUseCase: IsVppIdBannerAllowedUseCase,
    private val hideVppIdBannerUseCase: HideVppIdBannerUseCase
) : ViewModel() {
    var state by mutableStateOf(NewAssessmentState())
        private set

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                state = state.copy(currentProfile = (profile as? Profile.StudentProfile).also {
                    it?.getGroupItem()
                    it?.getDefaultLessons()?.onEach { defaultLesson ->
                        defaultLesson.getTeacherItem()
                        defaultLesson.getCourseItem()
                        defaultLesson.getGroupItems()
                    }
                })
            }
        }
        viewModelScope.launch vppIdBanner@{
            isVppIdBannerAllowedUseCase().collectLatest { canShow ->
                state = state.copy(canShowVppIdBanner = canShow)
            }
        }
    }

    fun onEvent(event: NewAssessmentEvent) {
        viewModelScope.launch {
            when (event) {
                is NewAssessmentEvent.HideVppIdBanner -> hideVppIdBannerUseCase()
                is NewAssessmentEvent.SelectDefaultLesson -> state = state.copy(selectedDefaultLesson = event.defaultLesson)
                is NewAssessmentEvent.SelectDate -> state = state.copy(selectedDate = event.date)
                is NewAssessmentEvent.SetVisibility -> state = state.copy(isVisible = event.isVisible)
                is NewAssessmentEvent.UpdateDescription -> state = state.copy(description = event.description)
                is NewAssessmentEvent.AddFile -> {
                    val file = AttachedFile.fromFile(event.file)
                    state = state.copy(files = state.files + file)
                }
                is NewAssessmentEvent.UpdateFile -> {
                    state = state.copy(files = state.files.map { file -> if (file.platformFile.path.hashCode() == event.file.platformFile.path.hashCode()) event.file else file })
                }
                is NewAssessmentEvent.RemoveFile -> {
                    state = state.copy(files = state.files.filter { it.platformFile.path.hashCode() != event.file.platformFile.path.hashCode() })
                }
                NewAssessmentEvent.Save -> Unit
            }
        }
    }
}

data class NewAssessmentState(
    val currentProfile: Profile.StudentProfile? = null,
    val canShowVppIdBanner: Boolean = false,
    val selectedDefaultLesson: DefaultLesson? = null,
    val selectedDate: LocalDate? = null,
    val description: String = "",
    val isVisible: Boolean? = null,
    val files: List<AttachedFile> = emptyList(),
)

sealed class NewAssessmentEvent {
    data object HideVppIdBanner : NewAssessmentEvent()
    data class SelectDefaultLesson(val defaultLesson: DefaultLesson) : NewAssessmentEvent()
    data class SelectDate(val date: LocalDate) : NewAssessmentEvent()
    data class SetVisibility(val isVisible: Boolean) : NewAssessmentEvent()
    data class UpdateDescription(val description: String) : NewAssessmentEvent()
    data object Save : NewAssessmentEvent()

    data class AddFile(val file: PlatformFile) : NewAssessmentEvent()
    data class UpdateFile(val file: AttachedFile) : NewAssessmentEvent()
    data class RemoveFile(val file: AttachedFile) : NewAssessmentEvent()
}