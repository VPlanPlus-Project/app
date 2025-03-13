package plus.vplan.app.feature.search.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.feature.assessment.ui.components.detail.AssessmentDetailDrawer
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import plus.vplan.app.feature.grades.page.detail.ui.GradeDetailDrawer
import plus.vplan.app.feature.homework.ui.components.detail.HomeworkDetailDrawer
import plus.vplan.app.feature.main.MainScreen
import plus.vplan.app.feature.search.domain.model.Result
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.feature.search.ui.main.components.LessonRow
import plus.vplan.app.feature.search.ui.main.components.SearchBar
import plus.vplan.app.feature.search.ui.main.components.SearchStart
import plus.vplan.app.feature.search.ui.main.components.StartScreen
import plus.vplan.app.feature.search.ui.main.components.Title
import plus.vplan.app.ui.subjectIcon
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.blendColor
import plus.vplan.app.utils.getState
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.toName
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.lock
import vplanplus.composeapp.generated.resources.lock_open

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
                targetState = state.query.isBlank(),
                modifier = Modifier.fillMaxSize()
            ) { showPlaceholder ->
                if (showPlaceholder) SearchStart(
                    profile = state.currentProfile,
                    newItems = state.newItems,
                    onAssessmentClicked = { visibleAssessment = it },
                    onHomeworkClicked = { visibleHomework = it },
                    onOpenRoomSearchClicked = onRoomSearchClicked
                )
            }
        }
        SearchBar(
            value = state.query,
            selectedDate = state.selectedDate,
            focusRequester = searchBarFocusRequester,
            onQueryChange = { onEvent(SearchEvent.UpdateQuery(it)) },
            onSelectDate = { onEvent(SearchEvent.SelectDate(it)) }
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

    return

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {

        }
        if (state.query.isBlank()) StartScreen(
            currentProfile = state.currentProfile,
            homework = state.homework,
            latestAssessments = state.assessments,
            onHomeworkClick = { visibleHomework = it },
            onAssessmentClick = { visibleAssessment = it },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            val lessonScroller = rememberScrollState()

            state.results.forEach { (type, results) ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = type.toName(),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (type == Result.Grade && state.gradeLockState != GradeLockState.NotConfigured) {
                        TextButton(
                            onClick = {
                                when (state.gradeLockState) {
                                    GradeLockState.Locked -> onEvent(SearchEvent.RequestGradeUnlock)
                                    GradeLockState.Unlocked -> onEvent(SearchEvent.RequestGradeLock)
                                    else -> Unit
                                }
                            }
                        ) {
                            AnimatedContent(
                                targetState = state.gradeLockState
                            ) { gradeLockState ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter =
                                        if (gradeLockState == GradeLockState.Locked) painterResource(Res.drawable.lock_open)
                                        else painterResource(Res.drawable.lock),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    if (gradeLockState == GradeLockState.Locked) Text("Entsperren")
                                    else Text("Sperren")
                                }
                            }
                        }
                    }
                }
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
                        LessonRow(
                            referenceTime = state.currentTime,
                            lessons = result.lessons,
                            scrollState = lessonScroller
                        )
                    }
                    if (result is SearchResult.Homework) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { visibleHomework = result.homework.id }
                                .padding(horizontal = 8.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(24.dp)) {
                                    result.homework.subjectInstance?.collectAsResultingFlow()?.value?.let { subjectInstance ->
                                        Icon(
                                            painter = painterResource(subjectInstance.subject.subjectIcon()),
                                            modifier = Modifier.fillMaxSize(),
                                            contentDescription = null
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Row {
                                        Text(
                                            text = result.homework.subjectInstance?.collectAsResultingFlow()?.value?.subject ?: result.homework.group?.collectAsResultingFlow()?.value?.name ?: "Unbekannte Zuweisung",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = " $DOT ${result.homework.dueTo.format(regularDateFormat)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        text = when (result.homework) {
                                            is Homework.CloudHomework -> result.homework.createdByItem!!.name
                                            is Homework.LocalHomework -> result.homework.createdByProfileItem!!.name
                                        },
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            result.homework.taskItems!!.forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .padding(start = 16.dp, end = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = DOT,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = task.content,
                                        style = MaterialTheme.typography.bodySmall.let {
                                            if (state.currentProfile is Profile.StudentProfile && task.isDone(state.currentProfile)) it.copy(textDecoration = TextDecoration.LineThrough)
                                            else it
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (result is SearchResult.Assessment) {
                        val subject by result.assessment.subjectInstance.collectAsResultingFlow()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { visibleAssessment = result.assessment.id }
                                .padding(horizontal = 8.dp),
                        ) {
                            Text(result.assessment.subjectInstanceId.toString())
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(24.dp)) {
                                    subject?.let { subjectInstance ->
                                        Icon(
                                            painter = painterResource(subjectInstance.subject.subjectIcon()),
                                            modifier = Modifier.fillMaxSize(),
                                            contentDescription = null
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Row {
                                        Text(
                                            text = subject?.subject ?: "",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = " $DOT ${result.assessment.date.format(regularDateFormat)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        text = when (result.assessment.creator) {
                                            is AppEntity.VppId -> result.assessment.createdByVppId!!.name
                                            is AppEntity.Profile -> result.assessment.createdByProfile!!.name
                                        },
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            Text(
                                text = result.assessment.description,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 32.dp, end = 8.dp)
                            )
                        }
                    }

                    if (result is SearchResult.Grade) {
                        val subject = result.grade.subject.getState(null).value
                        val collection = result.grade.collection.getState(null).value
                        val interval = collection?.interval?.getState(null)?.value

                        val red = colors[CustomColor.Red]!!.getGroup()
                        val green = colors[CustomColor.Green]!!.getGroup()

                        val backgroundColor = when (interval?.type) {
                            Interval.Type.SEK1 -> blendColor(blendColor(green.container, red.container, ((result.grade.numericValue?:1)-1)/5f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                            Interval.Type.SEK2 -> blendColor(blendColor(red.container, green.container, (result.grade.numericValue?:0)/15f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                            else -> Color.Transparent
                        }

                        val foregroundColor = when (interval?.type) {
                            Interval.Type.SEK1 -> blendColor(blendColor(green.onContainer, red.onContainer, ((result.grade.numericValue?:1)-1)/5f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                            Interval.Type.SEK2 -> blendColor(blendColor(red.onContainer, green.onContainer, (result.grade.numericValue?:0)/15f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                            else -> Color.Transparent
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { visibleGrade = result.grade.id }
                                .padding(horizontal = 8.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(24.dp)) {
                                    subject?.let { subject ->
                                        Icon(
                                            painter = painterResource(subject.localId.subjectIcon()),
                                            modifier = Modifier.fillMaxSize(),
                                            contentDescription = null
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Row {
                                        subject?.let {
                                            Text(
                                                text = it.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        collection?.let {
                                            Text(
                                                text = " $DOT ${collection.givenAt.format(regularDateFormat)}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.weight(1f, true))
                                AnimatedContent(
                                    targetState = state.gradeLockState
                                ) { gradeLockState ->
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .thenIf(Modifier.clickable { onEvent(SearchEvent.RequestGradeUnlock) }) { gradeLockState == GradeLockState.Locked }
                                            .background(if (gradeLockState == GradeLockState.Locked) MaterialTheme.colorScheme.outline else backgroundColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (gradeLockState == GradeLockState.Locked) Icon(
                                            painter = painterResource(Res.drawable.lock_open),
                                            modifier = Modifier.size(12.dp),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outlineVariant
                                        ) else Text(
                                            text = buildString {
                                                if (result.grade.isOptional) append("(")
                                                if (result.grade.value != null) append(result.grade.value)
                                                else append("-")
                                                if (result.grade.isOptional) append(")")
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = foregroundColor
                                        )
                                    }
                                }
                            }
                            collection?.let {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 32.dp, end = 8.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}