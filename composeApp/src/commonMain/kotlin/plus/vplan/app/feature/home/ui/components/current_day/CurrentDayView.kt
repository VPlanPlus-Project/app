package plus.vplan.app.feature.home.ui.components.current_day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.feature.home.ui.HomeViewDay
import plus.vplan.app.feature.home.ui.components.DayInfoCard
import plus.vplan.app.feature.home.ui.components.FollowingLessons
import plus.vplan.app.feature.home.ui.components.SectionTitle
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.utils.takeContinuousBy
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.lightbulb

private val subtitleDateFormat = LocalDate.Format {
    dayOfWeek(
        DayOfWeekNames(
            "Montag",
            "Dienstag",
            "Mittwoch",
            "Donnerstag",
            "Freitag",
            "Samstag",
            "Sonntag"
        )
    )
    chars(", ")
    dayOfMonth(Padding.NONE)
    chars(". ")
    monthName(
        MonthNames(
            "Januar",
            "Februar",
            "März",
            "April",
            "Mai",
            "Juni",
            "Juli",
            "August",
            "September",
            "Oktober",
            "November",
            "Dezember"
        )
    )
    chars(" ")
    year()
}

private val subtitleTimeFormat = LocalTime.Format {
    hour(Padding.NONE)
    char(':')
    minute()
}

@Composable
fun CurrentDayView(
    day: HomeViewDay,
    contextTime: LocalDateTime
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var currentLessons = remember { emptyList<Pair<Lesson, List<Lesson>>>() }
        LaunchedEffect("${contextTime.hour}:${contextTime.minute}") {
            val contextZoned = contextTime.toInstant(TimeZone.currentSystemDefault())
            currentLessons = day.timetable.ifEmpty { day.timetable }
                .filter {
                    val lessonTime = it.getLessonTimeItem()
                    val start =
                        lessonTime.start.atDate(day.day.date).toInstant(TimeZone.of("Europe/Berlin"))
                    val end = lessonTime.end.atDate(day.day.date).toInstant(TimeZone.of("Europe/Berlin"))
                    contextZoned in start..end
                }
                .map { currentLesson ->
                    currentLesson to day.timetable.ifEmpty { day.timetable }
                        .filter {
                            it.subject == currentLesson.subject
                                    && (it.teachers - currentLesson.teachers.toSet()).size != it.teachers.size
                                    && ((it.rooms.orEmpty() - currentLesson.rooms.orEmpty()
                                .toSet()).size != it.rooms?.size || it.rooms
                                .isEmpty() || currentLesson.rooms.orEmpty().isEmpty())
                                    && it.defaultLesson == currentLesson.defaultLesson
                                    && it.getLessonTimeItem().lessonNumber > currentLesson.getLessonTimeItem().lessonNumber
                        }.sortedBy { it.lessonTimeItem!!.lessonNumber }
                        .takeContinuousBy { it.lessonTimeItem!!.lessonNumber }
                }
        }
        if (currentLessons.isNotEmpty()) Column currentLessons@{
            SectionTitle(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Aktuelle Stunde" + if (currentLessons.size > 1) "n" else "",
                subtitle = "${
                    currentLessons.map { it.first.lessonTimeItem!!.lessonNumber }.distinct().sorted()
                        .joinToString { "$it." }
                } Stunde"
            )

            if (currentLessons.all { it.first.subject == null }) {
                val nextActualLesson = day.timetable.ifEmpty { day.timetable }
                    .firstOrNull { lesson -> lesson.subject != null && lesson.lessonTimeItem!!.start > currentLessons.maxOf { it.first.lessonTimeItem!!.end } }
                InfoCard(
                    imageVector = Res.drawable.lightbulb,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                    title = "Ausfall",
                    text = "Außerplanmäßiger Stundenausfall bis ${
                        nextActualLesson?.lessonTimeItem?.start?.format(
                            subtitleTimeFormat
                        ) ?: "Ende des Tages"
                    }",
                )
            }

            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                currentLessons.forEach { (currentLessons, followingConnected) ->
                    CurrentLessonCard(currentLessons, followingConnected, contextTime)
                }
            }
        }

        Column yourDay@{
            SectionTitle(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Dein Tag",
                subtitle = "${day.day.date.format(remember { subtitleDateFormat })}, bis ${
                    day.timetable.ifEmpty { day.timetable }
                        .maxOf { it.lessonTimeItem!!.end }.format(remember { subtitleTimeFormat })
                }"
            )
            if (day.day.info != null) DayInfoCard(Modifier.padding(vertical = 4.dp), info = day.day.info)
            val followingLessons = day.timetable.ifEmpty { day.timetable }
                .filter { it.lessonTimeItem!!.lessonNumber > (currentLessons.lastOrNull()?.first?.lessonTimeItem?.lessonNumber ?: Int.MAX_VALUE) }
                .sortedBy { it.lessonTimeItem!!.start }
            if (followingLessons.isNotEmpty()) Column {
                val lessonsGroupedByLessonNumber =
                    followingLessons.groupBy { it.lessonTimeItem!!.lessonNumber }
                FollowingLessons(
                    showFirstGradient =
                    lessonsGroupedByLessonNumber.keys.min() > (currentLessons.minOfOrNull { it.first.lessonTimeItem!!.lessonNumber }
                        ?: -1),
                    date = day.day.date,
                    lessons = lessonsGroupedByLessonNumber
                )
            } else Column {
                FollowingLessons(
                    showFirstGradient = false,
                    date = day.day.date,
                    lessons = day.timetable.ifEmpty { day.timetable }.groupBy { it.lessonTimeItem!!.lessonNumber }
                )
            }
        }
    }
}