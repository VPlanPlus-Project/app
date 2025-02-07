package plus.vplan.app.feature.search.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    var state by mutableStateOf(SearchState())
        private set

    fun onEvent(event: SearchEvent) {
        viewModelScope.launch {
            when (event) {
                is SearchEvent.UpdateQuery -> state = state.copy(query = event.query)
            }
        }
    }
}

data class SearchState(
    val query: String = ""
)

sealed class SearchEvent {
    data class UpdateQuery(val query: String): SearchEvent()
}