@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.feature.grades.page.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.CollectionsRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.populated.besteschule.CollectionPopulator
import plus.vplan.app.domain.model.populated.besteschule.IntervalPopulator
import plus.vplan.app.domain.model.populated.besteschule.PopulatedCollection
import plus.vplan.app.domain.model.populated.besteschule.PopulatedInterval
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
    private val besteSchuleCollectionsRepository: CollectionsRepository,
    private val lockGradesUseCase: LockGradesUseCase,
    private val intervalPopulator: IntervalPopulator,
    private val collectionPopulator: CollectionPopulator,
) : ViewModel(), KoinComponent {
    val state: StateFlow<GradeDetailState>
        field = MutableStateFlow(GradeDetailState())

    private val profileRepository by inject<ProfileRepository>()
    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()

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
                gradeCollection = null,
                gradeInterval = null,
                gradeUser = null,
            )
        }
        mainJob?.cancel()
        mainJob = viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { profile ->
                if (profile !is Profile.StudentProfile) return@collectLatest

                besteSchuleGradesRepository.getGradeFromCache(gradeId)
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
                                    state.update { state ->
                                        state.copy(
                                            grade = grade,
                                            gradeUser = user,
                                            initDone = true,
                                        )
                                    }
                                }

                            besteSchuleCollectionsRepository.getById(grade.collectionId)
                                .filterNotNull()
                                .flatMapLatest { collectionPopulator.populateSingle(it) }
                                .collectLatest { collection ->
                                    state.update { state ->
                                        state.copy(gradeCollection = collection)
                                    }

                                    coroutineScope {
                                        intervalPopulator.populateSingle(collection.interval)
                                            .onEach { interval ->
                                                state.update { state ->
                                                    state.copy(gradeInterval = interval)
                                                }
                                            }
                                            .launchIn(this)
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
                    besteSchuleGradesRepository.addGradesToCache(
                        listOf(
                            state.value.grade!!.copy(
                                isSelectedForFinalGrade = !state.value.grade!!.isSelectedForFinalGrade
                            )
                        )
                    )
                }
                is GradeDetailEvent.Reload -> {
                    state.update { state ->
                        state.copy(reloadingState = UnoptimisticTaskState.InProgress)
                    }
                    val result = updateGradeUseCase(
                        gradeId = state.value.grade!!.id,
                        schulverwalterAccessToken = state.value.gradeUser!!.schulverwalterConnection!!.accessToken,
                        schulverwalterUserId = state.value.gradeUser!!.schulverwalterConnection!!.userId
                    )
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

data class GradeDetailState(
    val grade: BesteSchuleGrade? = null,
    val gradeCollection: PopulatedCollection? = null,
    val gradeInterval: PopulatedInterval? = null,
    val gradeUser: VppId.Active? = null,
    val profile: Profile.StudentProfile? = null,
    val initDone: Boolean = false,
    val reloadingState: UnoptimisticTaskState? = null,
    val lockState: GradeLockState? = null,
)

sealed class GradeDetailEvent {
    data object ToggleConsiderForFinalGrade : GradeDetailEvent()
    data object Reload : GradeDetailEvent()

    data object RequestGradesUnlock: GradeDetailEvent()
    data object LockGrades: GradeDetailEvent()
}