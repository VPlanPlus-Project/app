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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import plus.vplan.app.feature.grades.domain.usecase.LockGradesUseCase
import plus.vplan.app.feature.grades.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.grades.domain.usecase.UpdateGradeUseCase
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState

class GradeDetailViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val updateGradeUseCase: UpdateGradeUseCase,
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val requestGradeUnlockUseCase: RequestGradeUnlockUseCase,
    private val lockGradesUseCase: LockGradesUseCase
) : ViewModel(), KoinComponent {
    var state by mutableStateOf(GradeDetailState())
        private set

    private val profileRepository by inject<ProfileRepository>()
    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()

    private var mainJob: Job? = null

    fun init(gradeId: Int) {
        state = GradeDetailState()
        mainJob?.cancel()
        mainJob = viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                getGradeLockStateUseCase(),
                besteSchuleGradesRepository.getGradeFromCache(gradeId)
            ) { profile, gradeLockState, grade ->
                if (profile !is Profile.StudentProfile) return@combine null

                profile.prefetch()

                state.copy(
                    grade = grade,
                    gradeUser = profileRepository.getAll()
                        .first()
                        .filterIsInstance<Profile.StudentProfile>()
                        .filter { it.vppIdId != null }
                        .map { it.vppId!!.getFirstValueOld() }
                        .filterIsInstance<VppId.Active>()
                        .filter { it.schulverwalterConnection != null }
                        .firstOrNull { it.schulverwalterConnection?.userId == grade?.schulverwalterUserId },
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
                is GradeDetailEvent.ToggleConsiderForFinalGrade -> {
                    besteSchuleGradesRepository.addGradesToCache(
                        listOf(
                            state.grade!!.copy(
                                isSelectedForFinalGrade = !state.grade!!.isSelectedForFinalGrade
                            )
                        )
                    )
                }
                is GradeDetailEvent.Reload -> {
                    state = state.copy(reloadingState = UnoptimisticTaskState.InProgress)
                    val result = updateGradeUseCase(
                        gradeId = state.grade!!.id,
                        schulverwalterAccessToken = state.gradeUser!!.schulverwalterConnection!!.accessToken,
                        schulverwalterUserId = state.gradeUser!!.schulverwalterConnection!!.userId
                    )
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
    val grade: BesteSchuleGrade? = null,
    val gradeUser: VppId.Active? = null,
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