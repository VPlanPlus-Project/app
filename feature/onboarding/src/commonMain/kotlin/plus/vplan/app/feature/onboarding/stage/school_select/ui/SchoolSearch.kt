package plus.vplan.app.feature.onboarding.stage.school_select.ui

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
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.feature.onboarding.stage.school_select.domain.usecase.OnboardingSchoolOption
import plus.vplan.app.feature.onboarding.stage.school_select.ui.components.OnboardingSchoolSearchHead
import plus.vplan.app.feature.onboarding.stage.school_select.ui.components.SearchBar
import plus.vplan.app.feature.onboarding.stage.school_select.ui.components.search_results.Error
import plus.vplan.app.feature.onboarding.stage.school_select.ui.components.search_results.SearchResults

@Composable
fun SchoolSearch(
    onSchoolSelected: (school: OnboardingSchoolOption) -> Unit
) {
    val viewModel = koinViewModel<SchoolSearchViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.handleEvent(SchoolSearchEvent.OnScreenBecameActive) }

    SchoolSearchContent(
        state = state,
        onEvent = viewModel::handleEvent
    )

    LaunchedEffect(state.isSchoolActivelySelected) {
        if (state.isSchoolActivelySelected) onSchoolSelected(state.selected!!)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SchoolSearchContent(
    state: OnboardingSchoolSearchState,
    onEvent: (SchoolSearchEvent) -> Unit,
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
                    .padding(top = 32.dp, bottom = 24.dp)
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
                visible = state.results is SchoolResults.Loading && state.searchQuery.isNotBlank(),
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
                visible = state.results is SchoolResults.Error && state.searchQuery.isNotBlank(),
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
                visible = state.results is SchoolResults.Results && state.searchQuery.isNotBlank(),
                enter = fadeIn() + slideInVertically { it/3 },
                exit = fadeOut()
            ) {
                SearchResults(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .padding(horizontal = 16.dp),
                    query = state.searchQuery,
                    results = (state.results as SchoolResults.Results).results,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SchoolSearchPreview() {
    AppTheme(dynamicColor = false) {
        SchoolSearchContent(
            state = OnboardingSchoolSearchState(),
            onEvent = {}
        )
    }
}

@Preview
@Composable
private fun SchoolSearchLoadingPreview() {
    AppTheme(dynamicColor = false) {
        SchoolSearchContent(
            state = OnboardingSchoolSearchState(
                searchQuery = "Test",
                results = SchoolResults.Loading
            ),
            onEvent = {}
        )
    }
}

@Preview
@Composable
private fun SchoolSearchErrorPreview() {
    AppTheme(dynamicColor = false) {
        SchoolSearchContent(
            state = OnboardingSchoolSearchState(
                searchQuery = "Test",
                results = SchoolResults.Error
            ),
            onEvent = {}
        )
    }
}

@Preview
@Composable
private fun SchoolSearchResultsPreview() {
    AppTheme(dynamicColor = false) {
        SchoolSearchContent(
            state = OnboardingSchoolSearchState(
                searchQuery = "Test",
                results = SchoolResults.Results(
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
}

@Preview
@Composable
private fun SchoolSearchNoResultsPreview() {
    AppTheme(dynamicColor = false) {
        SchoolSearchContent(
            state = OnboardingSchoolSearchState(
                searchQuery = "Test",
                results = SchoolResults.Results(emptyList()),
            ),
            onEvent = {}
        )
    }
}