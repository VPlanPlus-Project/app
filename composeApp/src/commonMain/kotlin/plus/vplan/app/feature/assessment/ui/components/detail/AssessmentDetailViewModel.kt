package plus.vplan.app.feature.assessment.ui.components.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.assessment.domain.usecase.AddAssessmentFileUseCase
import plus.vplan.app.feature.assessment.domain.usecase.ChangeAssessmentContentUseCase
import plus.vplan.app.feature.assessment.domain.usecase.ChangeAssessmentDateUseCase
import plus.vplan.app.feature.assessment.domain.usecase.ChangeAssessmentTypeUseCase
import plus.vplan.app.feature.assessment.domain.usecase.ChangeAssessmentVisibilityUseCase
import plus.vplan.app.feature.assessment.domain.usecase.DeleteAssessmentUseCase
import plus.vplan.app.feature.assessment.domain.usecase.UpdateAssessmentUseCase
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult
import plus.vplan.app.feature.homework.domain.usecase.DeleteFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.DownloadFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.RenameFileUseCase
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState
import plus.vplan.app.ui.common.AttachedFile

class AssessmentDetailViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val updateAssessmentUseCase: UpdateAssessmentUseCase,
    private val deleteAssessmentUseCase: DeleteAssessmentUseCase,
    private val changeAssessmentTypeUseCase: ChangeAssessmentTypeUseCase,
    private val changeAssessmentDateUseCase: ChangeAssessmentDateUseCase,
    private val changeAssessmentVisibilityUseCase: ChangeAssessmentVisibilityUseCase,
    private val changeAssessmentContentUseCase: ChangeAssessmentContentUseCase,
    private val addAssessmentFileUseCase: AddAssessmentFileUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val renameFileUseCase: RenameFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase
) : ViewModel() {
    var state by mutableStateOf(AssessmentDetailState())
        private set

    private var mainJob: Job? = null

    fun init(assessmentId: Int) {
        state = AssessmentDetailState()
        mainJob?.cancel()
        mainJob = viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                App.assessmentSource.getById(assessmentId)
            ) { profile, assessmentData ->
                if (assessmentData !is CacheState.Done || profile !is Profile.StudentProfile) return@combine null
                val assessment = assessmentData.data

                assessment.prefetch()
                profile.prefetch()

                val isOtherAssessment = state.assessment?.id != assessmentId

                state.copy(
                    assessment = assessment,
                    profile = profile,
                    canEdit = (assessment.creator is AppEntity.VppId && profile.vppIdId == assessment.creator.id) || (assessment.creator is AppEntity.Profile && profile.id == assessment.creator.id),
                    reloadingState = if (isOtherAssessment) null else state.reloadingState,
                    deleteState = if (isOtherAssessment) null else state.deleteState,
                    initDone = true
                )
            }.filterNotNull().collectLatest { state = it }
        }
    }

    fun onEvent(event: AssessmentDetailEvent) {
        viewModelScope.launch {
            when (event) {
                is AssessmentDetailEvent.Reload -> {
                    state = state.copy(reloadingState = UnoptimisticTaskState.InProgress)
                    val result = updateAssessmentUseCase(state.assessment!!.id)
                    when (result) {
                        UpdateResult.SUCCESS -> {
                            state = state.copy(reloadingState = UnoptimisticTaskState.Success)
                            viewModelScope.launch {
                                delay(2000)
                                if (state.reloadingState == UnoptimisticTaskState.Success) state = state.copy(reloadingState = null)
                            }
                        }
                        UpdateResult.ERROR -> state = state.copy(reloadingState = UnoptimisticTaskState.Error)
                        UpdateResult.DOES_NOT_EXIST -> state = state.copy(reloadingState = UnoptimisticTaskState.Success, deleteState = UnoptimisticTaskState.Success)
                    }
                }
                is AssessmentDetailEvent.Delete -> {
                    state = state.copy(deleteState = UnoptimisticTaskState.InProgress)
                    val result = deleteAssessmentUseCase(state.assessment!!, state.profile!!)
                    state = state.copy(deleteState = if (result) UnoptimisticTaskState.Success else UnoptimisticTaskState.Error)
                }
                is AssessmentDetailEvent.UpdateType -> changeAssessmentTypeUseCase(state.assessment!!, event.type, state.profile!!)
                is AssessmentDetailEvent.UpdateDate -> changeAssessmentDateUseCase(state.assessment!!, event.date, state.profile!!)
                is AssessmentDetailEvent.UpdateVisibility -> changeAssessmentVisibilityUseCase(state.assessment!!, event.isPublic, state.profile!!)
                is AssessmentDetailEvent.UpdateContent -> changeAssessmentContentUseCase(state.assessment!!, event.content, state.profile!!)
                is AssessmentDetailEvent.AddFile -> addAssessmentFileUseCase(state.assessment!!, event.file.platformFile, state.profile!!)
                is AssessmentDetailEvent.DownloadFile -> {
                    downloadFileUseCase(event.file, state.profile!!).collectLatest {
                        state = state.copy(fileDownloadState = state.fileDownloadState.plus(event.file.id to it))
                    }
                    state = state.copy(fileDownloadState = state.fileDownloadState - event.file.id)
                }
                is AssessmentDetailEvent.RenameFile -> renameFileUseCase(event.file, event.newName, state.profile!!)
                is AssessmentDetailEvent.DeleteFile -> deleteFileUseCase(event.file, state.profile!!)
            }
        }
    }
}

data class AssessmentDetailState(
    val assessment: Assessment? = null,
    val profile: Profile.StudentProfile? = null,
    val canEdit: Boolean = false,
    val reloadingState: UnoptimisticTaskState? = UnoptimisticTaskState.Success,
    val deleteState: UnoptimisticTaskState? = null,
    val initDone: Boolean = false,
    val fileDownloadState: Map<Int, Float> = emptyMap()
)

private suspend fun Profile.StudentProfile.prefetch() {
    this.getGroupItem()
    this.getSubjectInstances().onEach {
        it.getCourseItem()
        it.getTeacherItem()
    }
}

private suspend fun Assessment.prefetch() {
    when (this.creator) {
        is AppEntity.VppId -> this.getCreatedByVppIdItem()
        is AppEntity.Profile -> this.getCreatedByProfileItem()!!.getGroupItem()
    }
}

sealed class AssessmentDetailEvent {
    data class AddFile(val file: AttachedFile): AssessmentDetailEvent()
    data class UpdateType(val type: Assessment.Type): AssessmentDetailEvent()
    data class UpdateVisibility(val isPublic: Boolean): AssessmentDetailEvent()
    data class UpdateDate(val date: LocalDate): AssessmentDetailEvent()
    data class DownloadFile(val file: File): AssessmentDetailEvent()
    data class RenameFile(val file: File, val newName: String): AssessmentDetailEvent()
    data class DeleteFile(val file: File): AssessmentDetailEvent()

    data class UpdateContent(val content: String): AssessmentDetailEvent()

    data object Reload : AssessmentDetailEvent()
    data object Delete : AssessmentDetailEvent()
}