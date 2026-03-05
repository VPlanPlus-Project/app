package plus.vplan.app.feature.onboarding

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.loading_data.domain.usecase.FetchProfileOptionsUseCase
import plus.vplan.app.feature.onboarding.stage.profile_selection.domain.usecase.SelectProfileUseCase
import plus.vplan.app.feature.onboarding.stage.school_select.domain.usecase.OnboardingSchoolOption

internal class OnboardingViewModel(
    private val fetchProfileOptionsUseCase: FetchProfileOptionsUseCase,
    private val selectProfileUseCase: SelectProfileUseCase,
): ViewModel() {
    val state: StateFlow<OnboardingState>
        field = MutableStateFlow(OnboardingState())

    val backStack = mutableStateListOf<Onboarding>(Onboarding.Welcome)

    fun reset() {
        state.value = OnboardingState()
    }

    fun navigateBack() {
        backStack.removeLastOrNull()
    }

    fun navigateToSchoolSelect() {
        backStack.add(Onboarding.SchoolSelect)
    }

    fun onSchoolSelected(selectedSchool: OnboardingSchoolOption) {
        state.update { it.copy(selectedSchool = selectedSchool) }
        backStack.add(Onboarding.SchoolCredentials)
    }

    private var initDataJob: Job? = null
    fun onCredentialsProvided(username: String, password: String) {
        state.update {
            it.copy(
                username = username,
                password = password
            )
        }
        backStack.add(Onboarding.LoadingData)

        initDataJob?.cancel()
        initDataJob = viewModelScope.launch {
            val options = fetchProfileOptionsUseCase(state.value.selectedSchool!!.sp24Id!!, username, password)

            state.update { it.copy(profileOptions = options) }
            backStack.add(Onboarding.ProfileSelection)
        }
    }

    /**
     * Called when the user selects a profile from the profile list.
     * - If teacher: immediately saves the profile and navigates to Permissions.
     * - If student: stores the selection and navigates to SubjectInstanceSelection.
     */
    fun onProfileSelected(profile: OnboardingProfile) {
        if (profile is OnboardingProfile.TeacherProfile) {
            viewModelScope.launch {
                selectProfileUseCase(profile)
                backStack.add(Onboarding.Permissions)
            }
        } else {
            state.update { it.copy(selectedOnboardingProfile = profile) }
            backStack.add(Onboarding.SubjectInstanceSelection)
        }
    }

    /**
     * Called after the student has configured subject instances.
     */
    fun onSubjectInstanceSelectionDone() {
        backStack.add(Onboarding.Permissions)
    }

    fun onPermissionsDone() {
        backStack.add(Onboarding.Finished)
    }
}

data class OnboardingState(
    val selectedSchool: OnboardingSchoolOption? = null,
    val username: String = "",
    val password: String = "",
    val profileOptions: List<OnboardingProfile> = emptyList(),
    val selectedOnboardingProfile: OnboardingProfile? = null,
)
