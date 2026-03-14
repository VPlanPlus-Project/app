@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.feature.profile.page.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.common.usecase.GetCurrentProfileUseCase
import plus.vplan.app.core.data.besteschule.GradesRepository
import plus.vplan.app.core.data.besteschule.IntervalsRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.domain.model.populated.besteschule.IntervalPopulator
import plus.vplan.app.domain.usecase.SetCurrentProfileUseCase
import plus.vplan.app.feature.grades.common.domain.model.GradeUiItem
import plus.vplan.app.feature.grades.common.domain.usecase.CalculateAverageUseCase
import plus.vplan.app.feature.grades.common.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.common.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.grades.page.view.ui.GradesItem
import plus.vplan.app.feature.profile.page.domain.usecase.GetProfilesUseCase
import plus.vplan.app.feature.profile.page.domain.usecase.HasVppIdLinkedUseCase

class ProfileViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val setCurrentProfileUseCase: SetCurrentProfileUseCase,
    private val getProfilesUseCase: GetProfilesUseCase,
    private val hasVppIdLinkedUseCase: HasVppIdLinkedUseCase,
    private val calculateAverageUseCase: CalculateAverageUseCase,
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val requestGradeUnlockUseCase: RequestGradeUnlockUseCase
) : ViewModel(), KoinComponent {
    var state by mutableStateOf(ProfileState())
        private set

    private val besteSchuleGradesRepository by inject<GradesRepository>()
    private val besteSchuleIntervalsRepository by inject<IntervalsRepository>()

    private val intervalPopulator by inject<IntervalPopulator>()

    init {
        viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                getProfilesUseCase(),
                hasVppIdLinkedUseCase(),
                getGradeLockStateUseCase()
            ) { currentProfile, profiles, hasVppIdLinked, areGradesLocked ->
                state.copy(
                    currentProfile = currentProfile,
                    profiles = profiles,
                    showVppIdBanner = !hasVppIdLinked,
                    areGradesLocked = !areGradesLocked.canAccess
                )
            }
            .collectLatest {
                state = it
                val profile = state.currentProfile
                if (!it.areGradesLocked && profile is Profile.StudentProfile) {
                    val vppId = profile.vppId ?: return@collectLatest

                    if (vppId.schulverwalterConnection != null) {
                        val intervals = besteSchuleIntervalsRepository.getAll().first()

                        state = state.copy(
                            currentInterval = intervals.firstOrNull { interval -> LocalDate.now() in interval.from..interval.to }
                        )

                        val grades = besteSchuleGradesRepository.getAllForUser(schulverwalterUserId = vppId.schulverwalterConnection!!.userId)
                            .first()
                            .map { grade ->
                                GradesItem(
                                    grade = grade,
                                    interval = intervalPopulator.populateSingle(grade.collection.interval).first()
                                )
                            }

                        state.currentInterval?.let { interval ->
                            state = state.copy(
                                averageGrade = calculateAverageUseCase(grades.map {
                                    GradeUiItem.ActualGrade(it.grade)
                                }, interval),
                                latestGrade = grades
                                    .filter { grade -> grade.grade.collection.interval.id == interval.id }
                                    .filterNot { grade -> grade.grade.value == null }
                                    .maxByOrNull { grade -> grade.grade.givenAt }
                                    ?.let { grade -> LatestGrade.Value(grade.grade.value!!) }
                                    ?: LatestGrade.NotExisting
                            )
                        }
                    }
                }
            }
        }
    }

    fun onEvent(event: ProfileScreenEvent) {
        viewModelScope.launch {
            when (event) {
                is ProfileScreenEvent.SetProfileSwitcherVisibility -> state = state.copy(isSheetVisible = event.to)
                is ProfileScreenEvent.SetActiveProfile -> setCurrentProfileUseCase(event.profile)
                is ProfileScreenEvent.RequestGradeUnlock -> requestGradeUnlockUseCase()
            }
        }
    }
}

data class ProfileState(
    val currentProfile: Profile? = null,
    val profiles: Map<School.AppSchool, List<Profile>> = emptyMap(),
    val showVppIdBanner: Boolean = false,

    val isSheetVisible: Boolean = false,

    val areGradesLocked: Boolean = false,
    val currentInterval: BesteSchuleInterval? = null,
    val averageGrade: Double? = null,
    val latestGrade: LatestGrade = LatestGrade.Loading,
)

sealed class ProfileScreenEvent {
    data class SetProfileSwitcherVisibility(val to: Boolean): ProfileScreenEvent()
    data class SetActiveProfile(val profile: Profile): ProfileScreenEvent()

    data object RequestGradeUnlock: ProfileScreenEvent()
}

sealed class LatestGrade {
    data object Loading: LatestGrade()
    data object NotExisting: LatestGrade()
    data class Value(val value: String): LatestGrade()
}