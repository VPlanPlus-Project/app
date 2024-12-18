package plus.vplan.app.feature.onboarding.stage.d_select_profile.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.DefaultLesson
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
                is OnboardingProfileSelectionEvent.SelectProfile -> {
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
    val defaultLessons: Map<DefaultLesson, Boolean> = emptyMap(),
)

sealed class OnboardingProfileSelectionEvent {
    data class SelectProfile(val profile: OnboardingProfile) : OnboardingProfileSelectionEvent()
    data object CommitProfile: OnboardingProfileSelectionEvent()
    data class ToggleDefaultLesson(val defaultLesson: DefaultLesson) : OnboardingProfileSelectionEvent()
}