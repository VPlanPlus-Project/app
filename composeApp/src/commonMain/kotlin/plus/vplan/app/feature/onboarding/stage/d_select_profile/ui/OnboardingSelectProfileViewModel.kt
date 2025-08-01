package plus.vplan.app.feature.onboarding.stage.d_select_profile.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.ProfileType
import plus.vplan.app.feature.onboarding.domain.usecase.GetOnboardingStateUseCase
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase.SelectProfileUseCase
import plus.vplan.app.ui.components.ButtonState

class OnboardingSelectProfileViewModel(
    private val selectProfileUseCase: SelectProfileUseCase,
    private val getOnboardingStateUseCase: GetOnboardingStateUseCase
) : ViewModel() {
    var state by mutableStateOf(OnboardingSelectProfileUiState())
        private set

    init {
        viewModelScope.launch {
            getOnboardingStateUseCase().collect { onboardingSp24State ->
                state = state.copy(options = onboardingSp24State.groupOptions.map {
                    OnboardingProfile.StudentProfile(
                        name = it,
                        subjectInstances = emptyList()
                    )
                })
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
                    if (event.profile !is OnboardingProfile.StudentProfile) {
                        state = state.copy(saveState = OnboardingProfileSelectionSaveState.IN_PROGRESS)
                        selectProfileUseCase(event.profile)
                        state = state.copy(saveState = OnboardingProfileSelectionSaveState.DONE)
                        return@launch
                    }
                    state = state.copy(
                        selectedProfile = event.profile,
                        subjectInstances = event.profile.subjectInstances.associateWith { true }
                    )
                }
                is OnboardingProfileSelectionEvent.ToggleSubjectInstance -> {
                    state = state.copy(
                        subjectInstances = state.subjectInstances.plus(event.subjectInstance to !state.subjectInstances[event.subjectInstance]!!)
                    )
                }
                is OnboardingProfileSelectionEvent.ToggleCourse -> {
                    val subjectInstances = state.subjectInstances.filterKeys { it.course == event.course.id }
                    val isCourseFullySelected = subjectInstances.values.all { it }
                    state = state.copy(
                        subjectInstances = state.subjectInstances.plus(subjectInstances.mapValues { !isCourseFullySelected })
                    )
                }
                is OnboardingProfileSelectionEvent.CommitProfile -> {
                    state = state.copy(saveState = OnboardingProfileSelectionSaveState.IN_PROGRESS)
                    selectProfileUseCase(state.selectedProfile!!, state.subjectInstances)
                    state = state.copy(saveState = OnboardingProfileSelectionSaveState.DONE)
                }
            }
        }
    }
}

data class OnboardingSelectProfileUiState(
    val options: List<OnboardingProfile> = emptyList(),
    val selectedProfile: OnboardingProfile? = null,
    val filterProfileType: ProfileType? = null,
    val subjectInstances: Map<SubjectInstance, Boolean> = emptyMap(),
    val saveState: OnboardingProfileSelectionSaveState = OnboardingProfileSelectionSaveState.NOT_STARTED
)

enum class OnboardingProfileSelectionSaveState {
    NOT_STARTED,
    IN_PROGRESS,
    DONE
}

fun OnboardingProfileSelectionSaveState.toButtonState(): ButtonState {
    return when (this) {
        OnboardingProfileSelectionSaveState.IN_PROGRESS -> ButtonState.Loading
        else -> ButtonState.Enabled
    }
}

sealed class OnboardingProfileSelectionEvent {
    data class SelectProfileType(val profileType: ProfileType?) : OnboardingProfileSelectionEvent()
    data class SelectProfile(val profile: OnboardingProfile?) : OnboardingProfileSelectionEvent()
    data object CommitProfile: OnboardingProfileSelectionEvent()
    data class ToggleSubjectInstance(val subjectInstance: SubjectInstance) : OnboardingProfileSelectionEvent()
    data class ToggleCourse(val course: Course) : OnboardingProfileSelectionEvent()
}