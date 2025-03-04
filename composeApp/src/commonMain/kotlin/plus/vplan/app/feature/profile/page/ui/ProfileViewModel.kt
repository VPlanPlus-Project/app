package plus.vplan.app.feature.profile.page.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.usecase.SetCurrentProfileUseCase
import plus.vplan.app.feature.grades.domain.usecase.CalculateAverageUseCase
import plus.vplan.app.feature.grades.domain.usecase.GetCurrentIntervalUseCase
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.profile.page.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.profile.page.domain.usecase.GetProfilesUseCase
import plus.vplan.app.feature.profile.page.domain.usecase.HasVppIdLinkedUseCase

class ProfileViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val setCurrentProfileUseCase: SetCurrentProfileUseCase,
    private val getProfilesUseCase: GetProfilesUseCase,
    private val hasVppIdLinkedUseCase: HasVppIdLinkedUseCase,
    private val getCurrentIntervalUseCase: GetCurrentIntervalUseCase,
    private val calculateAverageUseCase: CalculateAverageUseCase,
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val requestGradeUnlockUseCase: RequestGradeUnlockUseCase
) : ViewModel() {
    var state by mutableStateOf(ProfileState())
        private set

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
                    if (profile.getVppIdItem()?.gradeIds.orEmpty().isNotEmpty()) {
                        state = state.copy(currentInterval = getCurrentIntervalUseCase())
                        val grades = profile.getVppIdItem()?.gradeIds?.map { gradeId -> App.gradeSource.getById(gradeId).getFirstValue()!! } ?: emptyList()
                        state = state.copy(averageGrade = calculateAverageUseCase(grades, state.currentInterval!!))
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
    val profiles: Map<School, List<Profile>> = emptyMap(),
    val showVppIdBanner: Boolean = false,

    val isSheetVisible: Boolean = false,

    val areGradesLocked: Boolean = false,
    val currentInterval: Interval? = null,
    val averageGrade: Double? = null,
)

sealed class ProfileScreenEvent {
    data class SetProfileSwitcherVisibility(val to: Boolean): ProfileScreenEvent()
    data class SetActiveProfile(val profile: Profile): ProfileScreenEvent()

    data object RequestGradeUnlock: ProfileScreenEvent()
}