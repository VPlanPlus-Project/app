package plus.vplan.app.feature.onboarding.stage.a_school_search.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.data.Response
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.OnboardingSchoolOption
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.OnboardingSchoolSearchHead
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.SearchBar
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.search_results.Error
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.search_results.SearchResults

@Composable
fun OnboardingSchoolSearch(
    navController: NavHostController,
) {
    val viewModel = koinViewModel<OnboardingSchoolSearchViewModel>()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.init(navController)
    }
    OnboardingSchoolSearchContent(
        state = state,
        onEvent = viewModel::handleEvent
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OnboardingSchoolSearchContent(
    state: OnboardingSchoolSearchState,
    onEvent: (OnboardingSchoolSearchEvent) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        val layoutDirection = LocalLayoutDirection.current
        Column(
            modifier = Modifier
                .padding(
                    top = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding(),
                    start = WindowInsets.safeDrawing.asPaddingValues().calculateStartPadding(layoutDirection),
                    end = WindowInsets.safeDrawing.asPaddingValues().calculateEndPadding(layoutDirection)
                )
                .fillMaxWidth()
        ) {
            OnboardingSchoolSearchHead(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 8.dp)
                    .padding(horizontal = 16.dp)
            )
            SearchBar(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                query = state.searchQuery,
                textFieldError = state.textFieldError,
                onEvent = onEvent
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = state.results is Response.Loading && state.searchQuery.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = state.results is Response.Error && state.searchQuery.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Error()
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = state.results is Response.Success && state.searchQuery.isNotBlank(),
                enter = fadeIn() + slideInVertically { it/3 },
                exit = fadeOut()
            ) {
                SearchResults(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .padding(horizontal = 16.dp),
                    query = state.searchQuery,
                    results = (state.results as? Response.Success<List<OnboardingSchoolOption>>)?.data.orEmpty(),
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingSchoolSearchPreview() {
    OnboardingSchoolSearchContent(
        state = OnboardingSchoolSearchState(),
        onEvent = {}
    )
}

@Preview
@Composable
private fun OnboardingSchoolSearchLoadingPreview() {
    OnboardingSchoolSearchContent(
        state = OnboardingSchoolSearchState(
            searchQuery = "Test",
            results = Response.Loading
        ),
        onEvent = {}
    )
}

@Preview
@Composable
private fun OnboardingSchoolSearchErrorPreview() {
    OnboardingSchoolSearchContent(
        state = OnboardingSchoolSearchState(
            searchQuery = "Test",
            results = Response.Error.OnlineError.ConnectionError
        ),
        onEvent = {}
    )
}

@Preview
@Composable
private fun OnboardingSchoolSearchResultsPreview() {
    OnboardingSchoolSearchContent(
        state = OnboardingSchoolSearchState(
            searchQuery = "Test",
            results = Response.Success(
                listOf(
                    OnboardingSchoolOption(
                        id = 1,
                        name = "Max-Planck-Gymnasium",
                        sp24Id = 12345678
                    ),
                    OnboardingSchoolOption(
                        id = 2,
                        name = "Realschule am Europakanal",
                        sp24Id = 87654321
                    ),
                    OnboardingSchoolOption(
                        id = 3,
                        name = "Gymnasium bei St. Anna",
                        sp24Id = null
                    ),
                )
            )
        ),
        onEvent = {}
    )
}

@Preview
@Composable
private fun OnboardingSchoolSearchNoResultsPreview() {
    OnboardingSchoolSearchContent(
        state = OnboardingSchoolSearchState(
            searchQuery = "Test",
            results = Response.Success(emptyList())
        ),
        onEvent = {}
    )
}