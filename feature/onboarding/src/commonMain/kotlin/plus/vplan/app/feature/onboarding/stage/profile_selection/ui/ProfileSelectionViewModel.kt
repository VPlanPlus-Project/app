package plus.vplan.app.feature.onboarding.stage.profile_selection.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.Course
import plus.vplan.app.core.model.ProfileType
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.ui.components.ButtonState
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.profile_selection.domain.usecase.SelectProfileUseCase

internal class ProfileSelectionViewModel(
    private val selectProfileUseCase: SelectProfileUseCase,
) : ViewModel() {
    val state: StateFlow<ProfileSelectionState>
        field = MutableStateFlow(ProfileSelectionState())

    fun init(options: List<OnboardingProfile>) {
        state.update { it.copy(options = options) }
    }

    /**
     * Initialises subject instance toggles for a student profile when entering
     * the SubjectInstanceSelection stage.
     */
    fun initSubjectInstances(profile: OnboardingProfile.StudentProfile) {
        state.update {
            it.copy(
                selectedProfile = profile,
                subjectInstances = profile.subjectInstances.associateWith { true }
            )
        }
    }

    /**
     * After the parent has consumed [ProfileSelectionState.profileSelectedForParent],
     * call this to clear the one-shot event.
     */
    fun onProfileForwardedToParent() {
        state.update { it.copy(profileSelectedForParent = null) }
    }

    fun onEvent(event: ProfileSelectionEvent) {
        when (event) {
            is ProfileSelectionEvent.SelectProfileType ->
                state.update { it.copy(filterProfileType = event.profileType) }

            is ProfileSelectionEvent.SelectProfile -> {
                if (event.profile == null) return
                // Signal to parent (OnboardingViewModel) which profile was selected.
                // The parent decides whether to navigate to SubjectInstanceSelection (student)
                // or directly to Permissions (teacher).
                state.update { it.copy(profileSelectedForParent = event.profile) }
            }

            is ProfileSelectionEvent.ToggleSubjectInstance ->
                state.update {
                    it.copy(
                        subjectInstances = it.subjectInstances.plus(
                            event.subjectInstance to !it.subjectInstances[event.subjectInstance]!!
                        )
                    )
                }

            is ProfileSelectionEvent.ToggleCourse -> {
                val affected = state.value.subjectInstances.filterKeys { it.course?.id == event.course.id }
                val isCourseFullySelected = affected.values.all { it }
                state.update {
                    it.copy(subjectInstances = it.subjectInstances.plus(affected.mapValues { !isCourseFullySelected }))
                }
            }

            is ProfileSelectionEvent.CommitProfile -> {
                viewModelScope.launch(CoroutineName(this::class.qualifiedName + ".Action.Commit")) {
                    state.update { it.copy(saveState = ProfileSelectionSaveState.IN_PROGRESS) }
                    selectProfileUseCase(state.value.selectedProfile!!, state.value.subjectInstances)
                    state.update { it.copy(saveState = ProfileSelectionSaveState.DONE) }
                }
            }
        }
    }
}

internal data class ProfileSelectionState(
    val options: List<OnboardingProfile> = emptyList(),
    val selectedProfile: OnboardingProfile? = null,
    val profileSelectedForParent: OnboardingProfile? = null,
    val filterProfileType: ProfileType? = null,
    val subjectInstances: Map<SubjectInstance, Boolean> = emptyMap(),
    val saveState: ProfileSelectionSaveState = ProfileSelectionSaveState.NOT_STARTED,
)

internal enum class ProfileSelectionSaveState {
    NOT_STARTED, IN_PROGRESS, DONE;

    fun toButtonState(): ButtonState = when (this) {
        IN_PROGRESS -> ButtonState.Loading
        else -> ButtonState.Enabled
    }
}

internal sealed class ProfileSelectionEvent {
    data class SelectProfileType(val profileType: ProfileType?) : ProfileSelectionEvent()
    data class SelectProfile(val profile: OnboardingProfile?) : ProfileSelectionEvent()
    data object CommitProfile : ProfileSelectionEvent()
    data class ToggleSubjectInstance(val subjectInstance: SubjectInstance) : ProfileSelectionEvent()
    data class ToggleCourse(val course: Course) : ProfileSelectionEvent()
}
