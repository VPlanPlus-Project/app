package plus.vplan.app.feature.onboarding.stage.a_school_search.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.data.Response

@Composable
fun OnboardingSchoolSearch(
    navController: NavHostController
) {
    val viewModel = koinViewModel<OnboardingSchoolSearchViewModel>()
    LaunchedEffect(Unit) {
        viewModel.init(navController)
    }
    OnboardingSchoolSearchContent(
        state = viewModel.state,
        onEvent = viewModel::handleEvent
    )
}

@Composable
private fun OnboardingSchoolSearchContent(
    state: OnboardingSchoolSearchState,
    onEvent: (OnboardingSchoolSearchEvent) -> Unit,
) {
    Column {
        TextField(
            value = state.searchQuery,
            onValueChange = { onEvent(OnboardingSchoolSearchEvent.OnQueryChanged(it)) },
            label = { Text("Schulnummer") },
        )
        Button(
            onClick = { onEvent(OnboardingSchoolSearchEvent.OnUseIndiwareClicked) }
        ) {
            Text("Use Indiware")
        }
        when (state.results) {
            Response.Loading -> Text("Loading...")
            is Response.Success -> {
                state.results.data.forEach { school ->
                    Text("${school.name} (${school.id})")
                }
            }
            else -> Text("Error: ${state.results}")
        }
    }
}