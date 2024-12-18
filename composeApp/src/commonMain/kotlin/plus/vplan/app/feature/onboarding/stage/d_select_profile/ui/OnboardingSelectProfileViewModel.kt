package plus.vplan.app.feature.onboarding.stage.d_select_profile.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.ProfileType
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase.GetProfileOptionsUseCase
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase.SelectProfileUseCase

class OnboardingSelectProfileViewModel(
    private val getProfileOptionsUseCase: GetProfileOptionsUseCase,
    private val selectProfileUseCase: SelectProfileUseCase
) : ViewModel() {
    var state by mutableStateOf(OnboardingSelectProfileUiState())
        private set

    init {
        viewModelScope.launch {
            getProfileOptionsUseCase().collect {
                state = state.copy(options = it)
            }
        }
    }

    fun onEvent(event: OnboardingProfileSelectionEvent) {
        viewModelScope.launch {
            when (event) {
                is OnboardingProfileSelectionEvent.SelectProfileType -> {
                    state = state.copy(filterProfileType = event.profileType)
                }
                is OnboardingProfileSelectionEvent.SelectProfile -> {
                    if (event.profile == null) {
                        state = state.copy(selectedProfile = null)
                        return@launch
                    }
                    if (event.profile !is OnboardingProfile.StudentProfile) selectProfileUseCase(event.profile)
                    else state = state.copy(
                        selectedProfile = event.profile,
                        defaultLessons = event.profile.defaultLessons.associateWith { true }
                    )
                }
                is OnboardingProfileSelectionEvent.ToggleDefaultLesson -> {
                    state = state.copy(
                        defaultLessons = state.defaultLessons.plus(event.defaultLesson to !state.defaultLessons[event.defaultLesson]!!)
                    )
                }
                is OnboardingProfileSelectionEvent.ToggleCourse -> {
                    val defaultLessons = state.defaultLessons.filterKeys { it.course == event.course }
                    val isCourseFullySelected = defaultLessons.values.all { it }
                    state = state.copy(
                        defaultLessons = state.defaultLessons.plus(defaultLessons.mapValues { !isCourseFullySelected })
                    )
                }
                is OnboardingProfileSelectionEvent.CommitProfile -> {
                    selectProfileUseCase(state.selectedProfile!!, state.defaultLessons)
                }
            }
        }
    }
}

data class OnboardingSelectProfileUiState(
    val options: List<OnboardingProfile> = emptyList(),
    val selectedProfile: OnboardingProfile? = null,
    val filterProfileType: ProfileType? = null,
    val defaultLessons: Map<DefaultLesson, Boolean> = emptyMap(),
)

sealed class OnboardingProfileSelectionEvent {
    data class SelectProfileType(val profileType: ProfileType?) : OnboardingProfileSelectionEvent()
    data class SelectProfile(val profile: OnboardingProfile?) : OnboardingProfileSelectionEvent()
    data object CommitProfile: OnboardingProfileSelectionEvent()
    data class ToggleDefaultLesson(val defaultLesson: DefaultLesson) : OnboardingProfileSelectionEvent()
    data class ToggleCourse(val course: Course) : OnboardingProfileSelectionEvent()
}