package plus.vplan.app.feature.search.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.feature.search.ui.main.components.SearchBar
import plus.vplan.app.ui.keyboardAsState
import plus.vplan.app.utils.toName

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
            Spacer(Modifier.height(16.dp))
            state.results.forEach { (type, results) ->
                Text(
                    text = type.toName(),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                results.forEach { result ->
                    if (result is SearchResult.SchoolEntity) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = result.name,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = result.school.name,
                                modifier = Modifier.padding(end = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Text(result.toString())
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}