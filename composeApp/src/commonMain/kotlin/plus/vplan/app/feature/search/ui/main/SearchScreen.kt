package plus.vplan.app.feature.search.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import plus.vplan.app.feature.search.ui.main.components.SearchBar
import plus.vplan.app.ui.keyboardAsState

@Composable
fun SearchScreen(
    navHostController: NavHostController,
    contentPadding: PaddingValues,
    viewModel: SearchViewModel,
    onToggleBottomBar: (visible: Boolean) -> Unit
) {

    val isKeyboardVisible by keyboardAsState()
    LaunchedEffect(isKeyboardVisible) {
        onToggleBottomBar(!isKeyboardVisible)
    }

    SearchScreenContent(
        state = viewModel.state,
        onEvent = viewModel::onEvent,
        contentPadding = contentPadding
    )
}

@Composable
private fun SearchScreenContent(
    state: SearchState,
    onEvent: (event: SearchEvent) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .padding(top = contentPadding.calculateTopPadding())
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            SearchBar(
                state.query,
                { onEvent(SearchEvent.UpdateQuery(it)) }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            state.results.forEach { (type, results) ->
                Text(type.name)
                results.forEach { result ->
                    Text(result.toString())
                }
            }
        }
    }
}