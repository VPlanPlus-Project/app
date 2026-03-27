package plus.vplan.app.feature.onboarding.stage.school_select.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.core.ui.util.paddingvalues.copy
import plus.vplan.app.feature.onboarding.stage.school_select.domain.usecase.OnboardingSchoolOption
import plus.vplan.app.feature.onboarding.stage.school_select.ui.components.SearchBar
import plus.vplan.app.feature.onboarding.stage.school_select.ui.components.search_results.Error
import plus.vplan.app.feature.onboarding.stage.school_select.ui.components.search_results.SearchResults
import plus.vplan.app.feature.onboarding.ui.components.OnboardingHeader

@Composable
fun SchoolSearch(
    contentPadding: PaddingValues,
    onSchoolSelected: (school: OnboardingSchoolOption) -> Unit
) {
    val viewModel = koinViewModel<SchoolSearchViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.handleEvent(SchoolSearchEvent.OnScreenBecameActive) }

    SchoolSearchContent(
        state = state,
        contentPadding = contentPadding,
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
    contentPadding: PaddingValues,
    onEvent: (SchoolSearchEvent) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current

    val searchBarFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (state.suppressAutoKeyboardOnShow) return@LaunchedEffect
        searchBarFocusRequester.requestFocus()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(contentPadding.copy(bottom = 0.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            OnboardingHeader(
                title = "Finde deine Schule",
                subtitle = "Suche nach dem Namen oder der Stundenplan24.de-Schulnummer deiner Schule."
            )
            SearchBar(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                query = state.searchQuery,
                textFieldError = state.textFieldError,
                onEvent = onEvent,
                searchBarFocusRequester = searchBarFocusRequester
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
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
                enter = fadeIn() + slideInVertically { -it/3 },
                exit = fadeOut(),
                modifier = Modifier
                    .clipToBounds()
                    .fillMaxSize()
            ) {
                SearchResults(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    query = state.searchQuery,
                    results = (state.results as? SchoolResults.Results)?.results.orEmpty(),
                    contentPadding = PaddingValues(bottom = 16.dp + contentPadding.calculateBottomPadding()),
                    onUseSp24School = {
                        localFocusManager.clearFocus()
                        onEvent(SchoolSearchEvent.OnUseSp24SchoolClicked)
                    },
                    onSelectSchool = {
                        localFocusManager.clearFocus()
                        onEvent(SchoolSearchEvent.OnSchoolSelected(it))
                    }
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
            contentPadding = PaddingValues(bottom = 600.dp),
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
            contentPadding = PaddingValues(),
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
            contentPadding = PaddingValues(),
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
            contentPadding = PaddingValues(bottom = 600.dp),
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
            contentPadding = PaddingValues(),
            onEvent = {}
        )
    }
}