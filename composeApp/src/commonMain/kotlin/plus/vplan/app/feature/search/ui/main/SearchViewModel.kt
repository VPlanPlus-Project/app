package plus.vplan.app.feature.search.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.feature.search.domain.model.Result
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.feature.search.domain.usecase.SearchUseCase
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.now
import kotlin.time.Duration.Companion.days

class SearchViewModel(
    private val searchUseCase: SearchUseCase
) : ViewModel() {

    var state by mutableStateOf(SearchState())
        private set

    var searchJob: Job? = null

    fun onEvent(event: SearchEvent) {
        viewModelScope.launch {
            when (event) {
                is SearchEvent.UpdateQuery -> {
                    state = state.copy(query = event.query)
                    restartSearch()
                }
            }
        }
    }

    private fun restartSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchUseCase(state.query, LocalDate.now().minus(1.days)).collectLatest {
                state = state.copy(results = it)
            }
        }
    }
}

data class SearchState(
    val query: String = "",
    val results: Map<Result, List<SearchResult>> = emptyMap()
)

sealed class SearchEvent {
    data class UpdateQuery(val query: String): SearchEvent()
}