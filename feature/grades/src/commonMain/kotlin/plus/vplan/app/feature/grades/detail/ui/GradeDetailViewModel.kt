@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.feature.grades.detail.ui

import androidx.compose.runtime.Immutable
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.GradesRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.application.UnoptimisticTaskState
import plus.vplan.app.core.model.application.UpdateResult
import plus.vplan.app.core.model.besteschule.BesteSchuleGrade
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
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

    @NativeCoroutinesState
    val state: StateFlow<GradeDetailState>
        field = MutableStateFlow(viewModelScope, GradeDetailState())

    private val profileRepository by inject<ProfileRepository>()
    private val besteSchuleGradesRepository by inject<GradesRepository>()

    private var mainJob: Job? = null

    init {
        viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".Init.GradeLock")) gradeLock@{
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

                    profileRepository.getAll()
                        .first()
                        .filterIsInstance<Profile.StudentProfile>()
                        .mapNotNull { it.vppId }
                        .filter { it.schulverwalterConnection != null }
                        .firstOrNull { it.schulverwalterConnection?.userId == grade.schulverwalterUserId }
                        ?.let { user ->
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

    fun onEvent(event: GradeDetailEvent) {
        when (event) {
            is GradeDetailEvent.ToggleConsiderForFinalGrade -> {
                viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".Event.ToggleConsiderForFinalGrade")) {
                    besteSchuleGradesRepository.save(
                        state.value.grade!!.copy(
                            isSelectedForFinalGrade = !state.value.grade!!.isSelectedForFinalGrade
                        )
                    )
                }
            }

            is GradeDetailEvent.Reload -> {
                viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".Event.Reload")) {
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
            }

            is GradeDetailEvent.RequestGradesUnlock -> requestGradeUnlockUseCase()
            is GradeDetailEvent.LockGrades -> {
                viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".Event.LockGrades")) {
                    lockGradesUseCase()
                }
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
) {
    val title = buildString {
        val grade = this@GradeDetailState.grade ?: return@buildString
        val value = if (grade.isOptional) "(${grade.value})" else grade.value
        when (grade.collection.interval.type) {
            is BesteSchuleInterval.Type.Sek2 -> {
                if (grade.value == null) append("Note")
                else append("$value Notenpunkte")
            }

            else -> {
                append("Note")
                if (grade.value != null) append(" $value")
            }
        }
    }

    val subtitle = this.grade?.collection?.subject?.fullName
}

sealed class GradeDetailEvent {
    data object ToggleConsiderForFinalGrade : GradeDetailEvent()
    data object Reload : GradeDetailEvent()

    data object RequestGradesUnlock : GradeDetailEvent()
    data object LockGrades : GradeDetailEvent()
}