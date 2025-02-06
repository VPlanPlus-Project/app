package plus.vplan.app.feature.assessment.ui.components.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
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
import plus.vplan.app.feature.assessment.domain.usecase.UpdateAssessmentUseCase
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState
import plus.vplan.app.ui.common.AttachedFile

class DetailViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val updateAssessmentUseCase: UpdateAssessmentUseCase
) : ViewModel() {
    var state by mutableStateOf(DetailState())
        private set

    private var mainJob: Job? = null

    fun init(assessmentId: Int) {
        state = DetailState()
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

                state.copy(
                    assessment = assessment,
                    profile = profile,
                    canEdit = (assessment.creator is AppEntity.VppId && profile.vppId == assessment.creator.id) || (assessment.creator is AppEntity.Profile && profile.id == assessment.creator.id),
                    isReloading = false,
                    initDone = true
                )
            }.filterNotNull().collectLatest { state = it }
        }
    }

    fun onEvent(event: DetailEvent) {
        viewModelScope.launch {
            when (event) {
                is DetailEvent.Reload -> {
                    state = state.copy(isReloading = true)
                    updateAssessmentUseCase(state.assessment!!.id)
                    state = state.copy(isReloading = false)
                }
                else -> TODO()
            }
        }
    }
}

data class DetailState(
    val assessment: Assessment? = null,
    val profile: Profile.StudentProfile? = null,
    val canEdit: Boolean = false,
    val isReloading: Boolean = false,
    val deleteState: UnoptimisticTaskState? = null,
    val initDone: Boolean = false,
    val fileDownloadState: Map<Int, Float> = emptyMap()
)

private suspend fun Profile.StudentProfile.prefetch() {
    this.getGroupItem()
    this.getDefaultLessons().onEach {
        it.getCourseItem()
        it.getTeacherItem()
    }
}

private suspend fun Assessment.prefetch() {
    this.getSubjectInstanceItem()
    when (this.creator) {
        is AppEntity.VppId -> this.getCreatedByVppIdItem()
        is AppEntity.Profile -> this.getCreatedByProfileItem()!!.getGroupItem()
    }
}

sealed class DetailEvent {
    data class AddFile(val file: AttachedFile): DetailEvent()
    data class UpdateVisibility(val isPublic: Boolean): DetailEvent()
    data class UpdateDate(val date: LocalDate): DetailEvent()
    data class DownloadFile(val file: File): DetailEvent()
    data class RenameFile(val file: File, val newName: String): DetailEvent()
    data class DeleteFile(val file: File): DetailEvent()

    data object Reload : DetailEvent()
    data object Delete : DetailEvent()
}