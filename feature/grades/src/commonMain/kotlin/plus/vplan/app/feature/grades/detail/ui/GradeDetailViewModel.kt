@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.feature.grades.detail.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.GradesRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.application.UnoptimisticTaskState
import plus.vplan.app.core.model.application.UpdateResult
import plus.vplan.app.core.model.besteschule.BesteSchuleGrade
import plus.vplan.app.feature.grades.common.domain.model.GradeLockState
import plus.vplan.app.feature.grades.common.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.common.domain.usecase.LockGradesUseCase
import plus.vplan.app.feature.grades.common.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.grades.common.domain.usecase.UpdateGradeUseCase

class GradeDetailViewModel(
    private val updateGradeUseCase: UpdateGradeUseCase,
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val requestGradeUnlockUseCase: RequestGradeUnlockUseCase,
    private val lockGradesUseCase: LockGradesUseCase,
) : ViewModel(), KoinComponent {
    val state: StateFlow<GradeDetailState>
        field = MutableStateFlow(GradeDetailState())

    private val profileRepository by inject<ProfileRepository>()
    private val besteSchuleGradesRepository by inject<GradesRepository>()

    private var mainJob: Job? = null

    init {
        viewModelScope.launch gradeLock@{
            getGradeLockStateUseCase().collectLatest { lockState ->
                state.update { state ->
                    state.copy(lockState = lockState)
                }
            }
        }
    }

    fun init(gradeId: Int) {
        state.update { state ->
            state.copy(
                grade = null,
                gradeUser = null,
            )
        }
        mainJob?.cancel()
        mainJob = viewModelScope.launch(Dispatchers.Default) {
            besteSchuleGradesRepository.getById(gradeId)
                .collectLatest { grade ->
                    if (grade == null) return@collectLatest

                    coroutineScope {
                        profileRepository.getAll()
                            .first()
                            .filterIsInstance<Profile.StudentProfile>()
                            .mapNotNull { it.vppId }
                            .filter { it.schulverwalterConnection != null }
                            .firstOrNull { it.schulverwalterConnection?.userId == grade.schulverwalterUserId }
                            ?.let { user ->
                                withContext(Dispatchers.Main) {
                                    state.update { state ->
                                        state.copy(
                                            grade = grade,
                                            gradeUser = user,
                                            initDone = true,
                                        )
                                    }
                                }
                            }
                    }
                }
        }
    }

    fun onEvent(event: GradeDetailEvent) {
        viewModelScope.launch {
            when (event) {
                is GradeDetailEvent.ToggleConsiderForFinalGrade -> {
                    withContext(Dispatchers.Default) {
                        besteSchuleGradesRepository.save(
                            state.value.grade!!.copy(
                                isSelectedForFinalGrade = !state.value.grade!!.isSelectedForFinalGrade
                            )
                        )
                    }
                }

                is GradeDetailEvent.Reload -> {
                    state.update { state ->
                        state.copy(reloadingState = UnoptimisticTaskState.InProgress)
                    }
                    val result = updateGradeUseCase(gradeId = state.value.grade!!.id)
                    when (result) {
                        UpdateResult.SUCCESS -> {
                            state.update { state ->
                                state.copy(reloadingState = UnoptimisticTaskState.Success)
                            }
                            viewModelScope.launch {
                                delay(2000)
                                if (state.value.reloadingState == UnoptimisticTaskState.Success) {
                                    state.update { state -> state.copy(reloadingState = null) }
                                }
                            }
                        }

                        UpdateResult.ERROR -> {
                            state.update { state ->
                                state.copy(reloadingState = UnoptimisticTaskState.Error)
                            }
                        }

                        else -> Unit
                    }
                }

                is GradeDetailEvent.RequestGradesUnlock -> requestGradeUnlockUseCase()
                is GradeDetailEvent.LockGrades -> lockGradesUseCase()
            }
        }
    }
}

@Immutable
data class GradeDetailState(
    val grade: BesteSchuleGrade? = null,
    val gradeUser: VppId.Active? = null,
    val profile: Profile.StudentProfile? = null,
    val initDone: Boolean = false,
    val reloadingState: UnoptimisticTaskState? = null,
    val lockState: GradeLockState? = null,
)

sealed class GradeDetailEvent {
    data object ToggleConsiderForFinalGrade : GradeDetailEvent()
    data object Reload : GradeDetailEvent()

    data object RequestGradesUnlock : GradeDetailEvent()
    data object LockGrades : GradeDetailEvent()
}