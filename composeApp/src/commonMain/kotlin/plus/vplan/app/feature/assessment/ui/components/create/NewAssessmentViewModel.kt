package plus.vplan.app.feature.assessment.ui.components.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
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
    private val _state = MutableStateFlow(NewAssessmentState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                val vppId = (profile as? Profile.StudentProfile)?.vppId

                _state.value = _state.value.copy(
                    currentProfile = (profile as? Profile.StudentProfile).also {
                        it?.getSubjectInstances()?.onEach { subjectInstance ->
                            subjectInstance.getTeacherItem()
                            subjectInstance.getCourseItem()
                            subjectInstance.getGroupItems()
                        }
                    },
                    isVisible = if (vppId != null) true else null
                )
            }
        }
        viewModelScope.launch vppIdBanner@{
            isVppIdBannerAllowedUseCase().collectLatest { canShow ->
                _state.value = _state.value.copy(canShowVppIdBanner = canShow)
            }
        }
    }

    fun onEvent(event: NewAssessmentEvent) {
        viewModelScope.launch {
            when (event) {
                is NewAssessmentEvent.HideVppIdBanner -> hideVppIdBannerUseCase()
                is NewAssessmentEvent.SelectSubjectInstance -> _state.update { it.copy(selectedSubjectInstance = event.subjectInstance, showSubjectInstanceError = false) }
                is NewAssessmentEvent.SelectDate -> _state.update { it.copy(selectedDate = event.date, showDateError = false) }
                is NewAssessmentEvent.SetVisibility -> _state.update { it.copy(isVisible = event.isVisible) }
                is NewAssessmentEvent.UpdateDescription -> _state.update { it.copy(description = event.description, showContentError = it.showContentError && event.description.isBlank()) }
                is NewAssessmentEvent.AddFile -> {
                    val file = AttachedFile.fromFile(event.file)
                    _state.update { it.copy(files = it.files + file) }
                }
                is NewAssessmentEvent.UpdateFile -> _state.update { it.copy(files = it.files.map { file -> if (file.platformFile.path.hashCode() == event.file.platformFile.path.hashCode()) event.file else file }) }
                is NewAssessmentEvent.RemoveFile -> _state.update { it.copy(files = it.files.filter { file -> file.platformFile.path.hashCode() != event.file.platformFile.path.hashCode() }) }
                is NewAssessmentEvent.UpdateType -> _state.update { it.copy(type = event.type, showTypeError = false) }
                NewAssessmentEvent.Save -> {
                    _state.update { current ->
                        var newState = current
                        if (current.selectedDate == null) newState = newState.copy(showDateError = true)
                        if (current.description.isBlank()) newState = newState.copy(showContentError = true)
                        if (current.type == null) newState = newState.copy(showTypeError = true)
                        if (current.selectedSubjectInstance == null) newState = newState.copy(showSubjectInstanceError = true)
                        if (current.savingState == UnoptimisticTaskState.InProgress) return@update newState
                        newState = newState.copy(savingState = UnoptimisticTaskState.InProgress)
                        val success = run save@{
                            createAssessmentUseCase(
                                text = newState.description.trim().ifBlank { return@save false },
                                isPublic = newState.isVisible,
                                date = newState.selectedDate ?: return@save false,
                                subjectInstance = newState.selectedSubjectInstance ?: return@save false,
                                type = newState.type ?: return@save false,
                                selectedFiles = newState.files
                            )
                            return@save true
                        }
                        newState.copy(savingState = if (success) UnoptimisticTaskState.Success else UnoptimisticTaskState.Error)
                    }
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
) {
    val hasChanges: Boolean = description.isNotBlank() || selectedSubjectInstance != null || selectedDate != null || isVisible != null || type != null || files.isNotEmpty()
}

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