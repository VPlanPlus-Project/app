package plus.vplan.app.feature.search.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import plus.vplan.app.feature.assessment.ui.components.detail.AssessmentDetailDrawer
import plus.vplan.app.feature.grades.page.detail.ui.GradeDetailDrawer
import plus.vplan.app.feature.homework.ui.components.detail.HomeworkDetailDrawer
import plus.vplan.app.feature.main.MainScreen
import plus.vplan.app.feature.search.ui.main.components.SearchBar
import plus.vplan.app.feature.search.ui.main.components.SearchResults
import plus.vplan.app.feature.search.ui.main.components.SearchStart
import plus.vplan.app.feature.search.ui.main.components.Title

@Composable
fun SearchScreen(
    navHostController: NavHostController,
    contentPadding: PaddingValues,
    viewModel: SearchViewModel
) {
    LaunchedEffect(Unit) { viewModel.onEvent(SearchEvent.UpdateQuery("")) }

    SearchScreenContent(
        state = viewModel.state,
        onEvent = viewModel::onEvent,
        contentPadding = contentPadding,
        onRoomSearchClicked = { navHostController.navigate(MainScreen.RoomSearch) }
    )
}

@Composable
private fun SearchScreenContent(
    state: SearchState,
    onEvent: (event: SearchEvent) -> Unit,
    onRoomSearchClicked: () -> Unit,
    contentPadding: PaddingValues
) {
    if (state.currentProfile == null) return

    var visibleHomework by rememberSaveable<MutableState<Int?>> { mutableStateOf(null) }
    var visibleAssessment by rememberSaveable<MutableState<Int?>> { mutableStateOf(null) }
    var visibleGrade by rememberSaveable<MutableState<Int?>> { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        val searchBarFocusRequester = remember { FocusRequester() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, true)
        ) content@{
            Title(modifier = Modifier.padding(horizontal = 16.dp)) { try {
                searchBarFocusRequester.requestFocus()
            } catch (_: Exception) {} }
            Spacer(Modifier.size(8.dp))
            AnimatedContent(
                targetState = state.query.hasActiveFilters,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    if (state.query.query.isBlank()) slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) togetherWith slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up) + scaleOut()
                    else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down) togetherWith slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) + scaleOut()
                }
            ) { hasFiltersActive ->
                if (!hasFiltersActive) SearchStart(
                    profile = state.currentProfile,
                    newItems = state.newItems,
                    onAssessmentClicked = { visibleAssessment = it },
                    onHomeworkClicked = { visibleHomework = it },
                    onOpenRoomSearchClicked = onRoomSearchClicked
                ) else SearchResults(
                    date = state.query.date,
                    dayType = state.selectedDateType,
                    profile = state.currentProfile,
                    results = state.results,
                    onHomeworkClicked = { visibleHomework = it },
                    onAssessmentClicked = { visibleAssessment = it },
                    onGradeClicked = { visibleGrade = it }
                )
            }
        }
        SearchBar(
            value = state.query.query,
            selectedDate = state.query.date,
            focusRequester = searchBarFocusRequester,
            subjects = state.subjects,
            selectedSubject = state.query.subject,
            selectedAssessmentType = state.query.assessmentType,
            onQueryChange = { onEvent(SearchEvent.UpdateQuery(it)) },
            onSelectDate = { onEvent(SearchEvent.SelectDate(it)) },
            onSelectSubject = { onEvent(SearchEvent.FilterForSubject(it)) },
            onSelectAssessmentType = { onEvent(SearchEvent.FitlerForAssessmentType(it)) }
        )
    }

    visibleHomework?.let { HomeworkDetailDrawer(
        homeworkId = it,
        onDismiss = { visibleHomework = null }
    ) }

    visibleAssessment?.let { AssessmentDetailDrawer(
        assessmentId = it,
        onDismiss = { visibleAssessment = null }
    ) }

    visibleGrade?.let { GradeDetailDrawer(
        gradeId = it,
        onDismiss = { visibleGrade = null }
    ) }
}