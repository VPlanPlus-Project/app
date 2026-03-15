package plus.vplan.app.feature.onboarding.stage.school_select.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.application.network.ApiException
import plus.vplan.app.feature.onboarding.stage.school_select.domain.usecase.OnboardingSchoolOption
import plus.vplan.app.feature.onboarding.stage.school_select.domain.usecase.SearchForSchoolUseCase

class SchoolSearchViewModel(
    private val searchForSchoolUseCase: SearchForSchoolUseCase,
) : ViewModel() {

    val state: StateFlow<OnboardingSchoolSearchState>
        field = MutableStateFlow(OnboardingSchoolSearchState())

    init {
        viewModelScope.launch {
            try {
                searchForSchoolUseCase.init()
            } catch (_: ApiException) {
                state.update { it.copy(results = SchoolResults.Error) }
            }
        }
    }

    private var searchJob: Job? = null
    private fun search() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            state.update { it.copy(results = SchoolResults.Loading) }
            val response = try {
                Response.Success(searchForSchoolUseCase(state.value.searchQuery))
            } catch (_: Exception) {
                Response.Error.Other("Something went wrong")
            }
            state.update {
                it.copy(
                    results = if (response !is Response.Success) SchoolResults.Error else SchoolResults.Results(response.data),
                    textFieldError = null,
                )
            }
        }
    }

    fun handleEvent(event: SchoolSearchEvent) {
        viewModelScope.launch {
            when (event) {
                is SchoolSearchEvent.OnScreenBecameActive -> {
                    state.update { it.copy(isSchoolActivelySelected = false) }
                }
                is SchoolSearchEvent.OnQueryChanged -> {
                    state.update {
                        it.copy(
                            searchQuery = event.query,
                            textFieldError = null,
                        )
                    }
                    search()
                }
                is SchoolSearchEvent.OnUseSp24SchoolClicked -> {
                    if ((state.value.searchQuery.toIntOrNull() ?: 0) !in 10000000..99999999) {
                        state.update { it.copy(textFieldError = SchoolSearchTextFieldError.BadSp24Id) }
                        return@launch
                    }
                    state.update {
                        it.copy(
                            isSchoolActivelySelected = true,
                            selected = OnboardingSchoolOption(
                                id = null,
                                name  = "Unknown School",
                                sp24Id = state.value.searchQuery.toInt(),
                                searchOptimizedName = state.value.searchQuery
                            )
                        )
                    }
                }
                is SchoolSearchEvent.OnSchoolSelected -> {
                    if (event.school.sp24Id == null) {
                        state.update { it.copy(textFieldError = SchoolSearchTextFieldError.SchoolNotFound) }
                        return@launch
                    }
                    state.update {
                        it.copy(
                            isSchoolActivelySelected = true,
                            selected = event.school
                        )
                    }
                }
            }
        }
    }
}

data class OnboardingSchoolSearchState(
    val searchQuery: String = "",
    val results: SchoolResults = SchoolResults.Loading,
    val textFieldError: SchoolSearchTextFieldError? = null,
    val selected: OnboardingSchoolOption? = null,
    val isSchoolActivelySelected: Boolean = false,
)

sealed class SchoolResults {
    data object Loading: SchoolResults()
    data object Error: SchoolResults()
    data class Results(val results: List<OnboardingSchoolOption>): SchoolResults()
}

sealed class SchoolSearchEvent {

    data object OnScreenBecameActive : SchoolSearchEvent()

    data class OnQueryChanged(val query: String) : SchoolSearchEvent()
    data object OnUseSp24SchoolClicked : SchoolSearchEvent()
    data class OnSchoolSelected(val school: OnboardingSchoolOption) : SchoolSearchEvent()
}

sealed class SchoolSearchTextFieldError {
    data object BadSp24Id : SchoolSearchTextFieldError()
    data object SchoolNotFound : SchoolSearchTextFieldError()
}