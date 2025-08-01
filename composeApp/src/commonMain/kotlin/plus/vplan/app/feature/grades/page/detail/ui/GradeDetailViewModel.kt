package plus.vplan.app.feature.grades.page.detail.ui

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
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import plus.vplan.app.feature.grades.domain.usecase.LockGradesUseCase
import plus.vplan.app.feature.grades.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.grades.domain.usecase.ToggleConsiderGradeForFinalGradeUseCase
import plus.vplan.app.feature.grades.domain.usecase.UpdateGradeUseCase
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState

class GradeDetailViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val updateGradeUseCase: UpdateGradeUseCase,
    private val toggleConsiderGradeForFinalGradeUseCase: ToggleConsiderGradeForFinalGradeUseCase,
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val requestGradeUnlockUseCase: RequestGradeUnlockUseCase,
    private val lockGradesUseCase: LockGradesUseCase
) : ViewModel() {
    var state by mutableStateOf(GradeDetailState())
        private set

    private var mainJob: Job? = null

    fun init(gradeId: Int) {
        state = GradeDetailState()
        mainJob?.cancel()
        mainJob = viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                getGradeLockStateUseCase(),
                App.gradeSource.getById(gradeId)
            ) { profile, gradeLockState, gradeData ->
                if (gradeData !is CacheStateOld.Done || profile !is Profile.StudentProfile) return@combine null
                val grade = gradeData.data

                profile.prefetch()

                state.copy(
                    grade = grade,
                    profile = profile,
                    lockState = gradeLockState,
                    initDone = true
                )
            }.filterNotNull().collectLatest { state = it }
        }
    }

    fun onEvent(event: GradeDetailEvent) {
        viewModelScope.launch {
            when (event) {
                is GradeDetailEvent.ToggleConsiderForFinalGrade -> toggleConsiderGradeForFinalGradeUseCase(state.grade!!.id, !state.grade!!.isSelectedForFinalGrade)
                is GradeDetailEvent.Reload -> {
                    state = state.copy(reloadingState = UnoptimisticTaskState.InProgress)
                    val result = updateGradeUseCase(state.grade!!.id)
                    when (result) {
                        UpdateResult.SUCCESS -> {
                            state = state.copy(reloadingState = UnoptimisticTaskState.Success)
                            viewModelScope.launch {
                                delay(2000)
                                if (state.reloadingState == UnoptimisticTaskState.Success) state = state.copy(reloadingState = null)
                            }
                        }
                        UpdateResult.ERROR -> state = state.copy(reloadingState = UnoptimisticTaskState.Error)
                        else -> Unit
                    }
                }
                is GradeDetailEvent.RequestGradesUnlock -> requestGradeUnlockUseCase()
                is GradeDetailEvent.LockGrades -> lockGradesUseCase()
            }
        }
    }
}

data class GradeDetailState(
    val grade: Grade? = null,
    val profile: Profile.StudentProfile? = null,
    val initDone: Boolean = false,
    val reloadingState: UnoptimisticTaskState? = null,
    val lockState: GradeLockState? = null,
)

private suspend fun Profile.StudentProfile.prefetch() {
    this.getGroupItem()
    this.getSubjectInstances().onEach {
        it.getCourseItem()
        it.getTeacherItem()
    }
}

sealed class GradeDetailEvent {
    data object ToggleConsiderForFinalGrade : GradeDetailEvent()
    data object Reload : GradeDetailEvent()

    data object RequestGradesUnlock: GradeDetailEvent()
    data object LockGrades: GradeDetailEvent()
}