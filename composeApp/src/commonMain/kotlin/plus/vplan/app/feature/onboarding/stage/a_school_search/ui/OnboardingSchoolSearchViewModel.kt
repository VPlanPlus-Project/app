package plus.vplan.app.feature.onboarding.stage.a_school_search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plus.vplan.app.domain.data.Response
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.OnboardingSchoolOption
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.SearchForSchoolUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.SelectSp24SchoolUseCase
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen

class OnboardingSchoolSearchViewModel(
    private val searchForSchoolUseCase: SearchForSchoolUseCase,
    private val selectSp24SchoolUseCase: SelectSp24SchoolUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingSchoolSearchState())
    val state = _state.asStateFlow()

    private lateinit var navController: NavHostController

    init {
        viewModelScope.launch {
            searchForSchoolUseCase.init()
        }
    }

    fun init(navController: NavHostController) {
        this.navController = navController
    }

    private var searchJob: Job? = null
    private fun search() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(results = Response.Loading) }
            val response = searchForSchoolUseCase(_state.value.searchQuery)
            _state.update {
                it.copy(
                    results = response,
                    textFieldError = null,
                )
            }
        }
    }

    fun handleEvent(event: OnboardingSchoolSearchEvent) {
        viewModelScope.launch {
            when (event) {
                is OnboardingSchoolSearchEvent.OnQueryChanged -> {
                    _state.update {
                        it.copy(
                            searchQuery = event.query,
                            textFieldError = null,
                        )
                    }
                    search()
                }
                is OnboardingSchoolSearchEvent.OnUseSp24SchoolClicked -> {
                    if ((state.value.searchQuery.toIntOrNull() ?: 0) !in 10000000..99999999) {
                        _state.update { it.copy(textFieldError = OnboardingSchoolSearchTextFieldError.BadSp24Id) }
                        return@launch
                    }
                    selectSp24SchoolUseCase(state.value.searchQuery.toInt())
                    navController.navigate(OnboardingScreen.OnboardingScreenSp24Login)
                }
                is OnboardingSchoolSearchEvent.OnSchoolSelected -> {
                    if (event.school.sp24Id == null) {
                        _state.update { it.copy(textFieldError = OnboardingSchoolSearchTextFieldError.SchoolNotFound) }
                        return@launch
                    }
                    selectSp24SchoolUseCase(event.school.sp24Id)
                    navController.navigate(OnboardingScreen.OnboardingScreenSp24Login)
                }
            }
        }
    }
}

data class OnboardingSchoolSearchState(
    val searchQuery: String = "",
    val results: Response<List<OnboardingSchoolOption>> = Response.Loading,
    val textFieldError: OnboardingSchoolSearchTextFieldError? = null
)

sealed class OnboardingSchoolSearchEvent {
    data class OnQueryChanged(val query: String) : OnboardingSchoolSearchEvent()
    data object OnUseSp24SchoolClicked : OnboardingSchoolSearchEvent()
    data class OnSchoolSelected(val school: OnboardingSchoolOption) : OnboardingSchoolSearchEvent()
}

sealed class OnboardingSchoolSearchTextFieldError {
    data object BadSp24Id : OnboardingSchoolSearchTextFieldError()
    data object SchoolNotFound : OnboardingSchoolSearchTextFieldError()
}