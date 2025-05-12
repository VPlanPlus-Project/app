package plus.vplan.app.feature.onboarding.stage.a_school_search.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.SearchBar
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.search_results.SearchResults
import plus.vplan.app.feature.onboarding.stage.migrate.a_read.ui.ImportScreenViewModel
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen

@Composable
fun OnboardingSchoolSearch(
    navController: NavHostController,
) {
    val viewModel = koinViewModel<OnboardingSchoolSearchViewModel>()
    LaunchedEffect(ImportScreenViewModel.migrationText) {
        if (ImportScreenViewModel.migrationText != null) navController.navigate(OnboardingScreen.OnboardingImportStart) { popUpTo(0) }
    }
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
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        Column(Modifier.weight(1f)) {
            SearchResults(
                query = state.searchQuery,
                results = state.results,
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