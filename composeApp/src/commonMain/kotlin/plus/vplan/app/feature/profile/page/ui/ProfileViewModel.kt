package plus.vplan.app.feature.profile.page.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository
import plus.vplan.app.domain.usecase.SetCurrentProfileUseCase
import plus.vplan.app.feature.grades.domain.usecase.CalculateAverageUseCase
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.profile.page.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.profile.page.domain.usecase.GetProfilesUseCase
import plus.vplan.app.feature.profile.page.domain.usecase.HasVppIdLinkedUseCase
import plus.vplan.app.utils.now

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

    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()
    private val besteSchuleIntervalsRepository by inject<BesteSchuleIntervalsRepository>()

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
                    val vppId = profile.vppId?.getFirstValueOld() as? VppId.Active ?: return@collectLatest

                    if (vppId.schulverwalterConnection != null) {
                        val intervals = besteSchuleIntervalsRepository.getIntervals(
                            responsePreference = ResponsePreference.Fast,
                            contextBesteschuleUserId = vppId.schulverwalterConnection.userId,
                            contextBesteschuleAccessToken = vppId.schulverwalterConnection.accessToken
                        )
                            .filterIsInstance<Response.Success<List<BesteSchuleInterval>>>()
                            .map { response -> response.data }
                            .first()

                        state = state.copy(
                            currentInterval = intervals.firstOrNull { interval -> LocalDate.now() in interval.from..interval.to }
                        )

                        val grades = besteSchuleGradesRepository.getGrades(
                            responsePreference = ResponsePreference.Fast,
                            contextBesteschuleUserId = vppId.schulverwalterConnection.userId,
                            contextBesteschuleAccessToken = vppId.schulverwalterConnection.accessToken
                        )
                            .filterIsInstance<Response.Success<List<BesteSchuleGrade>>>()
                            .map { response -> response.data }
                            .first()

                        state.currentInterval?.let { interval ->
                            state = state.copy(
                                averageGrade = calculateAverageUseCase(grades, interval),
                                latestGrade = grades
                                    .filter { grade -> grade.collection.first()!!.intervalId == interval.id }
                                    .filterNot { grade -> grade.value == null }
                                    .maxByOrNull { grade -> grade.givenAt }
                                    ?.let { grade -> LatestGrade.Value(grade.value!!) }
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