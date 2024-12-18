package plus.vplan.app.feature.onboarding.stage.a_school_search.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.OnlineSchool
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.SearchForSchoolUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.UseUnknownSp24SchoolUseCase
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen

class OnboardingSchoolSearchViewModel(
    private val searchForSchoolUseCase: SearchForSchoolUseCase,
    private val useUnknownSp24SchoolUseCase: UseUnknownSp24SchoolUseCase
) : ViewModel() {
    var state by mutableStateOf(OnboardingSchoolSearchState())
        private set

    private lateinit var navController: NavHostController

    init {
        viewModelScope.launch {
            searchForSchoolUseCase.init()
        }
    }

    fun init(navController: NavHostController) {
        this.navController = navController
    }

    fun handleEvent(event: OnboardingSchoolSearchEvent) {
        viewModelScope.launch {
            when (event) {
                is OnboardingSchoolSearchEvent.OnQueryChanged -> {
                    state = state.copy(
                        searchQuery = event.query,
                        textFieldError = null
                    )
                    state = state.copy(results = searchForSchoolUseCase(event.query))
                }
                is OnboardingSchoolSearchEvent.OnUseIndiwareClicked -> {
                    if ((state.searchQuery.toIntOrNull() ?: 0) !in 10000000..99999999) {
                        state = state.copy(textFieldError = OnboardingSchoolSearchTextFieldError.BadSp24Id)
                        return@launch
                    }
                    useUnknownSp24SchoolUseCase(state.searchQuery.toInt())
                    navController.navigate(OnboardingScreen.OnboardingScreenIndiwareLogin)
                }
                is OnboardingSchoolSearchEvent.OnSchoolSelected -> {
                    if (event.school.sp24Id == null) {
                        state = state.copy(textFieldError = OnboardingSchoolSearchTextFieldError.SchoolNotFound)
                        return@launch
                    }
                    useUnknownSp24SchoolUseCase(event.school.sp24Id)
                    navController.navigate(OnboardingScreen.OnboardingScreenIndiwareLogin)
                }
            }
        }
    }
}

data class OnboardingSchoolSearchState(
    val searchQuery: String = "",
    val results: Response<List<OnlineSchool>> = Response.Loading,
    val textFieldError: OnboardingSchoolSearchTextFieldError? = null
)

sealed class OnboardingSchoolSearchEvent {
    data class OnQueryChanged(val query: String) : OnboardingSchoolSearchEvent()
    data object OnUseIndiwareClicked : OnboardingSchoolSearchEvent()
    data class OnSchoolSelected(val school: OnlineSchool) : OnboardingSchoolSearchEvent()
}

sealed class OnboardingSchoolSearchTextFieldError {
    data object BadSp24Id : OnboardingSchoolSearchTextFieldError()
    data object SchoolNotFound : OnboardingSchoolSearchTextFieldError()
}