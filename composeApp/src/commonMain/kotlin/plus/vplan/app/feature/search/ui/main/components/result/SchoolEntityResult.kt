package plus.vplan.app.feature.search.ui.main.components.result

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import plus.vplan.app.App
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.cache.collectAsResultingFlowOld
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.feature.calendar.ui.components.calendar.CalendarView
import plus.vplan.app.feature.calendar.ui.components.calendar.CalendarViewLessons
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.ui.components.LineShimmer
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.findCurrentLessons
import plus.vplan.app.utils.getNextLessonStart
import plus.vplan.app.utils.now
import plus.vplan.app.utils.regularTimeFormat

@Composable
fun SchoolEntityResults(
    contextDate: LocalDate,
    results: List<SearchResult.SchoolEntity>,
    onClick: (result: SearchResult.SchoolEntity) -> Unit
) {
    var schoolEntityNameWidth by remember { mutableStateOf(0.dp) }
    results.forEachIndexed { i, result ->
        key(result.id) {
            val (currentLessons, nextLesson, hasLessonsLoaded) = rememberLessonLoadResult(result, contextDate)
            val contextDateIsToday = contextDate == LocalDate.now()
            val hasCurrentLesson = currentLessons.isNotEmpty() && hasLessonsLoaded && contextDateIsToday

            if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            val isOnlyResult = results.size == 1
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isOnlyResult) { onClick(result) }
                    .padding(8.dp)
            ) result@{
                Column body@{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) head@{
                        ResultName(
                            name = result.name,
                            width = schoolEntityNameWidth,
                            onWidthIncrease = { newWidth ->
                                schoolEntityNameWidth = newWidth
                            }
                        )
                        Column title@{
                            if (!hasCurrentLesson && !hasLessonsLoaded) {
                                LineShimmer()
                                return@title
                            }
                            if (hasLessonsLoaded && !hasCurrentLesson && contextDateIsToday) Text(
                                text = getHeaderTextForTodayWithLessons(result.lessons.size),
                                style = MaterialTheme.typography.bodySmall
                            )
                            else {
                                when (result) {
                                    is SearchResult.SchoolEntity.Room -> {
                                        Text(
                                            text = getHeaderTextForRoomWithCurrentLessons(currentLessons),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    is SearchResult.SchoolEntity.Teacher, is SearchResult.SchoolEntity.Group -> {
                                        Text(
                                            text = getHeaderTextForTeacherOrGroupWithCurrentLessons(currentLessons),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                            if (!isOnlyResult || (currentLessons.isEmpty() && hasLessonsLoaded && nextLesson != null)) Row {
                                Text(
                                    text = buildString {
                                        if (!isOnlyResult) append("Tippe für alle Stunden ")
                                        if (currentLessons.isEmpty() && hasLessonsLoaded && nextLesson != null) {
                                            if (!isOnlyResult) append("$DOT ")
                                            append("Nächster Unterricht ab ${nextLesson.format(regularTimeFormat)}")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    if (isOnlyResult) {
                        CalendarView(
                            modifier = Modifier.padding(top = 8.dp),
                            profile = null,
                            dayType = Day.DayType.REGULAR,
                            date = contextDate,
                            lessons = CalendarViewLessons.CalendarView(result.lessons), // TODO
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
    }
}

data class LessonLoadResult(
    val currentLessons: SnapshotStateList<Lesson>,
    val nextLesson: LocalTime?,
    val hasLessonsLoaded: Boolean
)

@Composable
private fun rememberLessonLoadResult(
    result: SearchResult.SchoolEntity,
    contextDate: LocalDate
): LessonLoadResult {
    val currentLessons = remember { mutableStateListOf<Lesson>() }
    var nextLesson by remember { mutableStateOf<LocalTime?>(null) }
    var hasLessonsLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(result.lessons) {
        currentLessons.clear()
        if (contextDate == LocalDate.now()) {
            currentLessons.addAll(result.lessons.map { it.lesson }.findCurrentLessons(LocalTime.now()).toMutableStateList())
            nextLesson = result.lessons.map { it.lesson }.getNextLessonStart(LocalTime.now())
        }
        hasLessonsLoaded = true
    }

    return LessonLoadResult(currentLessons, nextLesson, hasLessonsLoaded)
}

private fun getHeaderTextForTodayWithLessons(lessons: Int) = when (lessons) {
    0 -> "Heute keine Stunden"
    1 -> "Eine Stunde heute"
    else -> "$lessons Stunden heute"
}

@Composable
private fun getHeaderTextForRoomWithCurrentLessons(lessons: List<Lesson>): String {
    val groups = lessons.map { it.groupIds }.flatten().distinct()
    return if (groups.isEmpty()) "Momentan nicht belegt (Keine Gruppen zugeteilt)"
    else "Momentan belegt von ${groups.map { App.groupSource.getById(it) }.collectAsResultingFlow().value.map { it.name }.sorted().joinToString()}"
}

@Composable
private fun getHeaderTextForTeacherOrGroupWithCurrentLessons(lessons: List<Lesson>): String {
    val rooms = lessons.flatMap { it.rooms.orEmpty() }.distinct()
    return if (rooms.isEmpty()) "Aktuell keine Stunde"
    else {
        val until = lessons.mapNotNull { it.lessonTime }.collectAsResultingFlowOld().value.maxOfOrNull { it.end }
        buildString {
            append("Momentan in ${rooms.map { it.name }.sorted().joinToString()}")
            if (until != null) append(" (bis $until)")
        }
    }
}