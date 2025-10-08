package plus.vplan.app.feature.search.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.calendar.ui.LessonLayoutingInfo
import plus.vplan.app.feature.calendar.ui.components.agenda.GradeCard
import plus.vplan.app.feature.calendar.ui.components.calendar.CalendarView
import plus.vplan.app.feature.calendar.ui.components.calendar.CalendarViewLessons
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.feature.search.ui.main.components.result.SchoolEntityResults
import plus.vplan.app.utils.now
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.safeBottomPadding
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.toName
import plus.vplan.app.utils.untilRelativeText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.book_open
import vplanplus.composeapp.generated.resources.door_closed
import vplanplus.composeapp.generated.resources.file_badge
import vplanplus.composeapp.generated.resources.notebook_text
import vplanplus.composeapp.generated.resources.search_x
import vplanplus.composeapp.generated.resources.square_user_round
import vplanplus.composeapp.generated.resources.users

@Composable
private fun sectionTitleFont() = MaterialTheme.typography.titleMedium

@Composable
fun SearchResults(
    isLoading: Boolean,
    profile: Profile,
    dayType: Day.DayType,
    date: LocalDate,
    results: Map<SearchResult.Type, List<SearchResult>>,
    onHomeworkClicked: (homeworkId: Int) -> Unit,
    onAssessmentClicked: (assessmentId: Int) -> Unit,
    onGradeClicked: (gradeId: Int) -> Unit
) {
    if (results.all { it.value.isEmpty() }) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }
            Icon(
                painter = painterResource(Res.drawable.search_x),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Keine Ergebnisse gefunden",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Versuche es mit einem anderen Suchbegriff",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    var visibleResult by remember { mutableStateOf<SearchResult.SchoolEntity?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        results.toList()
            .sortedBy { (type, _) -> typeTypeSortings.indexOf(type).let { if (it == -1) typeTypeSortings.size + 1 else it } }
            .filter { it.second.isNotEmpty() }
            .forEach { (type, results) ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(when (type) {
                            SearchResult.Type.Group -> Res.drawable.users
                            SearchResult.Type.Teacher -> Res.drawable.square_user_round
                            SearchResult.Type.Room -> Res.drawable.door_closed
                            SearchResult.Type.Homework -> Res.drawable.book_open
                            SearchResult.Type.Assessment -> Res.drawable.notebook_text
                            SearchResult.Type.Grade -> Res.drawable.file_badge
                        }),
                        contentDescription = null,
                        modifier = Modifier.size(sectionTitleFont().lineHeight.toDp())
                    )
                    Row {
                        Text(
                            text = type.toName(),
                            style = sectionTitleFont(),
                            modifier = Modifier.alignByBaseline()
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = "(${results.size})",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }
                Spacer(Modifier.size(4.dp))
                when (type) {
                    SearchResult.Type.Group, SearchResult.Type.Room, SearchResult.Type.Teacher  -> SchoolEntityResults(
                        contextDate = date,
                        results = results.filterIsInstance<SearchResult.SchoolEntity>(),
                        onClick = { visibleResult = it }
                    )
                    SearchResult.Type.Homework -> {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            results.filterIsInstance<SearchResult.Homework>().forEach { result ->
                                plus.vplan.app.feature.calendar.ui.components.agenda.HomeworkCard(
                                    homework = result.homework,
                                    profile = profile,
                                    onClick = { onHomeworkClicked(result.homework.id) }
                                )
                            }
                        }
                    }
                    SearchResult.Type.Assessment -> {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            results.filterIsInstance<SearchResult.Assessment>().forEach { result ->
                                plus.vplan.app.feature.calendar.ui.components.agenda.AssessmentCard(
                                    assessment = result.assessment,
                                    onClick = { onAssessmentClicked(result.assessment.id) }
                                )
                            }
                        }
                    }
                    SearchResult.Type.Grade -> {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            results.filterIsInstance<SearchResult.Grade>().forEach { result ->
                                GradeCard(
                                    grade = result.grade,
                                    onClick = { onGradeClicked(result.grade.id) }
                                )
                            }
                        }
                    }
                }
            }
    }

    visibleResult?.let {
        LessonsDrawer(
            date = date,
            lessons = it.lessons,
            dayType = dayType,
            type = it.type,
            name = it.name,
            onDismiss = { visibleResult = null }
        )
    }
}

private val typeTypeSortings = listOf(
    SearchResult.Type.Group,
    SearchResult.Type.Teacher,
    SearchResult.Type.Room,
    SearchResult.Type.Homework,
    SearchResult.Type.Assessment,
    SearchResult.Type.Grade,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonsDrawer(
    date: LocalDate,
    dayType: Day.DayType,
    lessons: List<LessonLayoutingInfo>,
    type: SearchResult.Type,
    name: String,
    onDismiss: () -> Unit
)  {
    val sheetState = rememberModalBottomSheetState(true)

    ModalBottomSheet(
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0.dp) },
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = safeBottomPadding())
            ) {
                Text(
                    text = buildString {
                        append(when (type) {
                            SearchResult.Type.Group -> "Klasse"
                            SearchResult.Type.Teacher -> "Lehrer"
                            SearchResult.Type.Room -> "Raum"
                            else -> "Anderes"
                        })
                        append(" ")
                        append(name)
                    },
                    style = MaterialTheme.typography.headlineLarge,
                )

                Text(
                    text = buildString {
                        append(when (lessons.size) {
                            0 -> "Keine Stunden"
                            1 -> "Eine Stunde"
                            else -> "${lessons.size} Stunden"
                        })
                        append(" für ")
                        append((LocalDate.now() untilRelativeText date) ?: date.format(regularDateFormat))
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                CalendarView(
                    profile = null,
                    dayType = dayType,
                    date = date,
                    lessons = CalendarViewLessons.CalendarView(lessons), // TODO
                    assessments = emptyList(),
                    homework = emptyList(),
                    autoLimitTimeSpanToLessons = true,
                    info = null,
                    contentScrollState = null,
                    onHomeworkClicked = {},
                    onAssessmentClicked = {}
                )
            }
        }
    }
}