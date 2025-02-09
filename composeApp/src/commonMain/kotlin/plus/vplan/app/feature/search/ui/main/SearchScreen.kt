package plus.vplan.app.feature.search.ui.main

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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.assessment.ui.components.detail.AssessmentDetailDrawer
import plus.vplan.app.feature.homework.ui.components.detail.HomeworkDetailDrawer
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.feature.search.ui.main.components.LessonRow
import plus.vplan.app.feature.search.ui.main.components.SearchBar
import plus.vplan.app.feature.search.ui.main.components.hourWidth
import plus.vplan.app.ui.keyboardAsState
import plus.vplan.app.ui.subjectIcon
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.toName

@Composable
fun SearchScreen(
    navHostController: NavHostController,
    contentPadding: PaddingValues,
    viewModel: SearchViewModel,
    onToggleBottomBar: (visible: Boolean) -> Unit
) {

    LaunchedEffect(Unit) { viewModel.onEvent(SearchEvent.UpdateQuery("")) }

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
    contentPadding: PaddingValues
) {
    val localDensity = LocalDensity.current
    var width by remember { mutableStateOf(0.dp) }

    var visibleHomework by rememberSaveable<MutableState<Int?>> { mutableStateOf(null) }
    var visibleAssessment by rememberSaveable<MutableState<Int?>> { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .padding(top = contentPadding.calculateTopPadding())
            .fillMaxSize()
            .onSizeChanged { with(localDensity) { width = it.width.toDp() } }
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

            val lessonScroller = rememberScrollState()
            LaunchedEffect(width) {
                lessonScroller.scrollTo(with(localDensity) { (state.currentTime.hour * 60 + state.currentTime.minute) * (hourWidth/60).roundToPx() - (width / 2).roundToPx() }.coerceAtLeast(0))
            }

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
                                    result.homework.defaultLessonItem?.let { defaultLesson ->
                                        Icon(
                                            painter = painterResource(defaultLesson.subject.subjectIcon()),
                                            modifier = Modifier.fillMaxSize(),
                                            contentDescription = null
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Row {
                                        Text(
                                            text = result.homework.defaultLessonItem?.subject ?: result.homework.groupItem?.name ?: "Unbekannte Zuweisung",
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { visibleAssessment = result.assessment.id }
                                .padding(horizontal = 8.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(24.dp)) {
                                    result.assessment.subjectInstanceItem?.let { defaultLesson ->
                                        Icon(
                                            painter = painterResource(defaultLesson.subject.subjectIcon()),
                                            modifier = Modifier.fillMaxSize(),
                                            contentDescription = null
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Row {
                                        Text(
                                            text = result.assessment.subjectInstanceItem!!.subject,
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

                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    visibleHomework?.let { HomeworkDetailDrawer(
        homeworkId = it,
        onDismiss = { visibleHomework = null }
    ) }

    visibleAssessment?.let { AssessmentDetailDrawer(
        assessmentId = it,
        onDismiss = { visibleAssessment = null }
    ) }
}