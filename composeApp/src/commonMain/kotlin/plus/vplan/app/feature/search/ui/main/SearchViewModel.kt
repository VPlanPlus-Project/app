package plus.vplan.app.feature.search.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.search.domain.model.Result
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.feature.search.domain.usecase.SearchUseCase

class SearchViewModel(
    private val searchUseCase: SearchUseCase,
    private val getCurrentDateTimeUseCase: GetCurrentDateTimeUseCase,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) : ViewModel() {

    var state by mutableStateOf(SearchState())
        private set

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            getCurrentDateTimeUseCase().collectLatest { state = state.copy(currentTime = it) }
        }

        viewModelScope.launch {
            getCurrentProfileUseCase().collectLatest { state = state.copy(currentProfile = it) }
        }
    }

    fun onEvent(event: SearchEvent) {
        viewModelScope.launch {
            when (event) {
                is SearchEvent.UpdateQuery -> {
                    state = state.copy(query = event.query)
                    restartSearch()
                }
                is SearchEvent.SelectDate -> {
                    state = state.copy(selectedDate = event.date)
                    restartSearch()
                }
            }
        }
    }

    private fun restartSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchUseCase(state.query, state.selectedDate).collectLatest {
                state = state.copy(results = it)
            }
        }
    }
}

data class SearchState(
    val query: String = "",
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val results: Map<Result, List<SearchResult>> = emptyMap(),
    val currentProfile: Profile? = null,
    val currentTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)

sealed class SearchEvent {
    data class UpdateQuery(val query: String): SearchEvent()
    data class SelectDate(val date: LocalDate): SearchEvent()
}