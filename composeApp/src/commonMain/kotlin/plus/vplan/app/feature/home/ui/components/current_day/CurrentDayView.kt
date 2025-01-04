package plus.vplan.app.feature.home.ui.components.current_day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.LessonTime
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
    day: Day,
    contextTime: LocalDateTime
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val currentLessons = remember("${contextTime.hour}:${contextTime.minute}") {
            val contextZoned = contextTime.toInstant(TimeZone.currentSystemDefault())
            day.substitutionPlan.ifEmpty { day.timetable }
                .filter {
                    if (!(it is Cacheable.Loaded && it.value.lessonTime is Cacheable.Loaded<LessonTime>)) return@filter false
                    val lessonTime = (it.value.lessonTime as? Cacheable.Loaded)?.value ?: return@filter false
                    val start =
                        lessonTime.start.atDate(day.date).toInstant(TimeZone.of("Europe/Berlin"))
                    val end = lessonTime.end.atDate(day.date).toInstant(TimeZone.of("Europe/Berlin"))
                    contextZoned in start..end
                }
                .filterIsInstance<Cacheable.Loaded<Lesson>>()
                .map { it.value }
                .map { currentLesson ->
                    currentLesson to day.substitutionPlan.ifEmpty { day.timetable }
                        .filterIsInstance<Cacheable.Loaded<Lesson>>()
                        .map { it.value }
                        .filter {
                            it.subject == currentLesson.subject
                                    && (it.teachers - currentLesson.teachers.toSet()).size != it.teachers.size
                                    && ((it.rooms.orEmpty() - currentLesson.rooms.orEmpty()
                                .toSet()).size != it.rooms?.size || it.rooms.orEmpty()
                                .isEmpty() || currentLesson.rooms.orEmpty().isEmpty())
                                    && it.defaultLesson == currentLesson.defaultLesson
                                    && it.lessonTime.toValueOrNull()!!.lessonNumber > currentLesson.lessonTime.toValueOrNull()!!.lessonNumber
                        }.sortedBy { it.lessonTime.toValueOrNull()!!.lessonNumber }
                        .takeContinuousBy { it.lessonTime.toValueOrNull()!!.lessonNumber }
            }
        }
        if (currentLessons.isNotEmpty()) Column currentLessons@{
            SectionTitle(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Aktuelle Stunde" + if (currentLessons.size > 1) "n" else "",
                subtitle = "${
                    currentLessons.map { it.first.lessonTime.toValueOrNull()!!.lessonNumber }.distinct().sorted()
                        .joinToString { "$it." }
                } Stunde"
            )

            if (currentLessons.all { it.first.subject == null }) {
                val nextActualLesson = day.substitutionPlan.ifEmpty { day.timetable }
                    .filterIsInstance<Cacheable.Loaded<Lesson>>()
                    .map { it.value }
                    .firstOrNull { lesson -> lesson.subject != null && lesson.lessonTime.toValueOrNull()!!.start > currentLessons.maxOf { it.first.lessonTime.toValueOrNull()!!.end } }
                InfoCard(
                    imageVector = Res.drawable.lightbulb,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                    title = "Ausfall",
                    text = "Außerplanmäßiger Stundenausfall bis ${
                        nextActualLesson?.lessonTime?.toValueOrNull()?.start?.format(
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
                subtitle = "${day.date.format(remember { subtitleDateFormat })}, bis ${
                    day.substitutionPlan.ifEmpty { day.timetable }
                        .mapNotNull { it.toValueOrNull() }
                        .maxOf { it.lessonTime.toValueOrNull()!!.end }.format(remember { subtitleTimeFormat })
                }"
            )
            if (day.info != null) DayInfoCard(Modifier.padding(vertical = 4.dp), info = day.info)
            val followingLessons = day.substitutionPlan.ifEmpty { day.timetable }
                .mapNotNull { it.toValueOrNull() }
                .filter { it.lessonTime.toValueOrNull()!!.lessonNumber > currentLessons.last().first.lessonTime.toValueOrNull()!!.lessonNumber }
                .sortedBy { it.lessonTime.toValueOrNull()!!.start }
            if (followingLessons.isNotEmpty()) Column {
                val lessonsGroupedByLessonNumber =
                    followingLessons.groupBy { it.lessonTime.toValueOrNull()!!.lessonNumber }
                FollowingLessons(
                    showFirstGradient =
                    lessonsGroupedByLessonNumber.keys.min() > (currentLessons.minOfOrNull { it.first.lessonTime.toValueOrNull()!!.lessonNumber }
                        ?: -1),
                    date = day.date,
                    lessons = lessonsGroupedByLessonNumber
                )
            }
        }
    }
}