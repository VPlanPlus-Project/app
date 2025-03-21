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
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.assessment.domain.usecase.CreateAssessmentUseCase
import plus.vplan.app.feature.homework.domain.usecase.HideVppIdBannerUseCase
import plus.vplan.app.feature.homework.domain.usecase.IsVppIdBannerAllowedUseCase
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState
import plus.vplan.app.ui.common.AttachedFile

class NewAssessmentViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val isVppIdBannerAllowedUseCase: IsVppIdBannerAllowedUseCase,
    private val hideVppIdBannerUseCase: HideVppIdBannerUseCase,
    private val createAssessmentUseCase: CreateAssessmentUseCase
) : ViewModel() {
    var state by mutableStateOf(NewAssessmentState())
        private set

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                state = state.copy(
                    currentProfile = (profile as? Profile.StudentProfile).also {
                        it?.getGroupItem()
                        it?.getSubjectInstances()?.onEach { subjectInstance ->
                            subjectInstance.getTeacherItem()
                            subjectInstance.getCourseItem()
                            subjectInstance.getGroupItems()
                        }
                    },
                    isVisible = if ((profile as? Profile.StudentProfile)?.getVppIdItem() != null) true else null
                )
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
                is NewAssessmentEvent.SelectSubjectInstance -> state = state.copy(selectedSubjectInstance = event.subjectInstance, showSubjectInstanceError = false)
                is NewAssessmentEvent.SelectDate -> state = state.copy(selectedDate = event.date, showDateError = false)
                is NewAssessmentEvent.SetVisibility -> state = state.copy(isVisible = event.isVisible)
                is NewAssessmentEvent.UpdateDescription -> state = state.copy(description = event.description, showContentError = state.showContentError && event.description.isBlank())
                is NewAssessmentEvent.AddFile -> {
                    val file = AttachedFile.fromFile(event.file)
                    state = state.copy(files = state.files + file)
                }
                is NewAssessmentEvent.UpdateFile -> state = state.copy(files = state.files.map { file -> if (file.platformFile.path.hashCode() == event.file.platformFile.path.hashCode()) event.file else file })
                is NewAssessmentEvent.RemoveFile -> state = state.copy(files = state.files.filter { it.platformFile.path.hashCode() != event.file.platformFile.path.hashCode() })
                is NewAssessmentEvent.UpdateType -> state = state.copy(type = event.type, showTypeError = false)
                NewAssessmentEvent.Save -> {
                    if (state.selectedDate == null) state = state.copy(showDateError = true)
                    if (state.description.isBlank()) state = state.copy(showContentError = true)
                    if (state.type == null) state = state.copy(showTypeError = true)
                    if (state.selectedSubjectInstance == null) state = state.copy(showSubjectInstanceError = true)
                    if (state.savingState == UnoptimisticTaskState.InProgress) return@launch
                    state = state.copy(savingState = UnoptimisticTaskState.InProgress)
                    run save@{
                        createAssessmentUseCase(
                            text = state.description.trim().ifBlank { return@save false },
                            isPublic = state.isVisible,
                            date = state.selectedDate ?: return@save false,
                            subjectInstance = state.selectedSubjectInstance ?: return@save false,
                            type = state.type ?: return@save false,
                            selectedFiles = state.files
                        )
                        return@save true
                    }.let { state = state.copy(savingState = if (it) UnoptimisticTaskState.Success else UnoptimisticTaskState.Error) }
                }
            }
        }
    }
}

data class NewAssessmentState(
    val currentProfile: Profile.StudentProfile? = null,
    val canShowVppIdBanner: Boolean = false,
    val selectedSubjectInstance: SubjectInstance? = null,
    val selectedDate: LocalDate? = null,
    val description: String = "",
    val isVisible: Boolean? = null,
    val type: Assessment.Type? = null,
    val files: List<AttachedFile> = emptyList(),

    val showContentError: Boolean = false,
    val showSubjectInstanceError: Boolean = false,
    val showDateError: Boolean = false,
    val showTypeError: Boolean = false,

    val savingState: UnoptimisticTaskState? = null
)

sealed class NewAssessmentEvent {
    data object HideVppIdBanner : NewAssessmentEvent()
    data class SelectSubjectInstance(val subjectInstance: SubjectInstance) : NewAssessmentEvent()
    data class SelectDate(val date: LocalDate) : NewAssessmentEvent()
    data class SetVisibility(val isVisible: Boolean) : NewAssessmentEvent()
    data class UpdateDescription(val description: String) : NewAssessmentEvent()
    data class UpdateType(val type: Assessment.Type) : NewAssessmentEvent()
    data object Save : NewAssessmentEvent()

    data class AddFile(val file: PlatformFile) : NewAssessmentEvent()
    data class UpdateFile(val file: AttachedFile) : NewAssessmentEvent()
    data class RemoveFile(val file: AttachedFile) : NewAssessmentEvent()
}