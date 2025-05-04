package plus.vplan.app.feature.onboarding.stage.a_school_search.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.SearchBar
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.search_results.SearchResults
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen

@Composable
fun OnboardingSchoolSearch(
    navController: NavHostController,
) {
    val viewModel = koinViewModel<OnboardingSchoolSearchViewModel>()
    LaunchedEffect(Unit) {
        viewModel.init(navController)
    }
    OnboardingSchoolSearchContent(
        state = viewModel.state,
        onImportFromOldAppClicked = remember { { navController.navigate(OnboardingScreen.OnboardingImportStart) } },
        onEvent = viewModel::handleEvent
    )
}

@Composable
private fun OnboardingSchoolSearchContent(
    state: OnboardingSchoolSearchState,
    onImportFromOldAppClicked: () -> Unit,
    onEvent: (OnboardingSchoolSearchEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        Column(Modifier.weight(1f)) {
            SearchResults(
                query = state.searchQuery,
                results = state.results,
                onImportFromOldAppClicked = onImportFromOldAppClicked,
                onEvent = onEvent
            )
        }
        SearchBar(
            query = state.searchQuery,
            textFieldError = state.textFieldError,
            onEvent = onEvent
        )
    }
}