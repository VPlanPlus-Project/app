@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.assessment.detail.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.common.usecase.GetCurrentProfileUseCase
import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.data.file.FileOperationProgress
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.application.UnoptimisticTaskState
import plus.vplan.app.core.model.application.UpdateResult
import plus.vplan.app.feature.assessment.core.domain.usecase.ChangeAssessmentContentUseCase
import plus.vplan.app.feature.assessment.core.domain.usecase.ChangeAssessmentDateUseCase
import plus.vplan.app.feature.assessment.core.domain.usecase.ChangeAssessmentTypeUseCase
import plus.vplan.app.feature.assessment.core.domain.usecase.ChangeAssessmentVisibilityUseCase
import plus.vplan.app.feature.assessment.core.domain.usecase.DeleteAssessmentUseCase
import plus.vplan.app.feature.assessment.core.domain.usecase.RefreshAssessmentUseCase
import plus.vplan.app.feature.file.core.domain.model.AttachedFile
import plus.vplan.app.feature.file.core.domain.usecase.DeleteFileUseCase
import plus.vplan.app.feature.file.core.domain.usecase.DownloadFileUseCase
import plus.vplan.app.feature.file.core.domain.usecase.GetFileThumbnailUseCase
import plus.vplan.app.feature.file.core.domain.usecase.OpenFileUseCase
import plus.vplan.app.feature.file.core.domain.usecase.RenameFileUseCase
import plus.vplan.app.feature.file.core.domain.usecase.UploadFileUseCase

class AssessmentDetailViewModel(
    private val assessmentRepository: AssessmentRepository,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val refreshAssessmentUseCase: RefreshAssessmentUseCase,
    private val deleteAssessmentUseCase: DeleteAssessmentUseCase,
    private val changeAssessmentTypeUseCase: ChangeAssessmentTypeUseCase,
    private val changeAssessmentDateUseCase: ChangeAssessmentDateUseCase,
    private val changeAssessmentVisibilityUseCase: ChangeAssessmentVisibilityUseCase,
    private val changeAssessmentContentUseCase: ChangeAssessmentContentUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val openFileUseCase: OpenFileUseCase,
    private val renameFileUseCase: RenameFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    val getFileThumbnailUseCase: GetFileThumbnailUseCase,
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
                assessmentRepository.getById(assessmentId).filterNotNull()
            ) { profile, assessment ->
                if (profile !is Profile.StudentProfile) return@combine null

                val isOtherAssessment = state.assessment?.id != assessmentId

                state.copy(
                    assessment = assessment,
                    profile = profile,
                    canEdit = (assessment.creator is AppEntity.VppId && profile.vppId?.id == (assessment.creator as AppEntity.VppId).vppId.id) || (assessment.creator is AppEntity.Profile && profile.id == (assessment.creator as AppEntity.Profile).profile.id),
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
                    val result = refreshAssessmentUseCase(state.assessment!!.id)
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
                is AssessmentDetailEvent.AddFile -> {
                    val profile = state.profile ?: return@launch
                    val assessment = state.assessment ?: return@launch
                    val vppId = profile.vppId ?: return@launch // Require VPP ID for upload
                    
                    when (val uploadResult = uploadFileUseCase(vppId, event.file.platformFile)) {
                        is Response.Success -> {
                            val uploadedFile = uploadResult.data
                            // Link the file to the assessment
                            if (assessment.id > 0) {
                                assessmentRepository.linkFile(vppId, assessment.id, uploadedFile.id)
                            }
                        }
                        is Response.Error, is Response.Loading -> {
                            // Handle error or loading state
                        }
                    }
                }
                is AssessmentDetailEvent.DownloadFile -> {
                    val profile = state.profile ?: return@launch
                    val schoolAuth = profile.vppId?.buildVppSchoolAuthentication(-1) ?: profile.school.buildSp24AppAuthentication()
                    downloadFileUseCase(event.file, schoolAuth).collectLatest { progress ->
                        state = state.copy(
                            fileOperationState = state.fileOperationState.plus(event.file.id to progress)
                        )
                    }
                }
                is AssessmentDetailEvent.OpenFile -> {
                    openFileUseCase(event.file)
                }
                is AssessmentDetailEvent.RenameFile -> {
                    renameFileUseCase(event.file, event.newName, state.profile?.vppId)
                }
                is AssessmentDetailEvent.DeleteFile -> {
                    deleteFileUseCase(event.file, state.profile?.vppId)
                }
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
    val fileOperationState: Map<Int, FileOperationProgress> = emptyMap()
)

sealed class AssessmentDetailEvent {
    data class AddFile(val file: AttachedFile): AssessmentDetailEvent()
    data class UpdateType(val type: Assessment.Type): AssessmentDetailEvent()
    data class UpdateVisibility(val isPublic: Boolean): AssessmentDetailEvent()
    data class UpdateDate(val date: LocalDate): AssessmentDetailEvent()
    data class DownloadFile(val file: File): AssessmentDetailEvent()
    data class OpenFile(val file: File): AssessmentDetailEvent()
    data class RenameFile(val file: File, val newName: String): AssessmentDetailEvent()
    data class DeleteFile(val file: File): AssessmentDetailEvent()

    data class UpdateContent(val content: String): AssessmentDetailEvent()

    data object Reload : AssessmentDetailEvent()
    data object Delete : AssessmentDetailEvent()
}