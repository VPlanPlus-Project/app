package plus.vplan.app.feature.onboarding

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.School
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.loading_data.domain.usecase.FetchAndStoreSchoolDataUseCase
import plus.vplan.app.feature.onboarding.stage.profile_selection.domain.usecase.BuildProfileOptionsFromLocalDataUseCase
import plus.vplan.app.feature.onboarding.stage.profile_selection.domain.usecase.SelectProfileUseCase
import plus.vplan.app.feature.onboarding.stage.school_select.domain.usecase.OnboardingSchoolOption

internal class OnboardingViewModel(
    private val fetchAndStoreSchoolDataUseCase: FetchAndStoreSchoolDataUseCase,
    private val buildProfileOptionsFromLocalDataUseCase: BuildProfileOptionsFromLocalDataUseCase,
    private val selectProfileUseCase: SelectProfileUseCase,
): ViewModel() {
    val state: StateFlow<OnboardingState>
        field = MutableStateFlow(OnboardingState())

    val backStack = mutableStateListOf<Onboarding>(Onboarding.Welcome)

    /** Replaces the entire backstack in one operation so it is never transiently empty. */
    private fun resetBackStack(vararg entries: Onboarding) {
        val new = entries.toList()
        new.forEachIndexed { i, entry ->
            if (i < backStack.size) backStack[i] = entry else backStack.add(entry)
        }
        while (backStack.size > new.size) backStack.removeLastOrNull()
    }

    fun reset() {
        state.value = OnboardingState()
        resetBackStack(Onboarding.Welcome)
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
            val options = fetchAndStoreSchoolDataUseCase(state.value.selectedSchool!!.sp24Id!!, username, password)

            state.update { it.copy(profileOptions = options) }
            backStack.add(Onboarding.ProfileSelection)
        }
    }

    /**
     * Called when the user adds a new profile to an already-configured school.
     * Skips school discovery, credential entry, and network fetching; reads profile
     * options directly from the local DB and jumps straight to ProfileSelection.
     */
    fun initWithSchool(school: School.AppSchool) {
        resetBackStack(Onboarding.LoadingData)
        state.update {
            it.copy(
                selectedSchool = OnboardingSchoolOption(
                    id = null,
                    name = school.name,
                    sp24Id = school.sp24Id.toIntOrNull()
                ),
                username = school.username,
                password = school.password,
            )
        }

        initDataJob?.cancel()
        initDataJob = viewModelScope.launch {
            val options = buildProfileOptionsFromLocalDataUseCase(school)
            state.update { it.copy(profileOptions = options) }
            backStack.add(Onboarding.ProfileSelection)
        }
    }

    /**
     * Called when the user selects a profile from the profile list.
     * - If teacher: stores the selection and navigates to TeacherNotice (selectProfileUseCase
     *   is deferred until after the Permissions screen is approved).
     * - If student: stores the selection and navigates to SubjectInstanceSelection.
     */
    fun onProfileSelected(profile: OnboardingProfile) {
        state.update { it.copy(selectedOnboardingProfile = profile) }
        if (profile is OnboardingProfile.TeacherProfile) {
            backStack.add(Onboarding.TeacherNotice)
        } else {
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
        val profile = state.value.selectedOnboardingProfile
        if (profile is OnboardingProfile.TeacherProfile) {
            viewModelScope.launch {
                selectProfileUseCase(profile)
                backStack.add(Onboarding.Finished)
            }
        } else {
            backStack.add(Onboarding.Finished)
        }
    }
}

data class OnboardingState(
    val selectedSchool: OnboardingSchoolOption? = null,
    val username: String = "",
    val password: String = "",
    val profileOptions: List<OnboardingProfile> = emptyList(),
    val selectedOnboardingProfile: OnboardingProfile? = null,
)
