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
import kotlinx.coroutines.flow.first
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
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
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
        val currentOrNextLesson = remember { mutableListOf<Pair<Lesson, List<Lesson>>>() }
        LaunchedEffect("${contextTime.hour}:${contextTime.minute}") {
            val contextZoned = contextTime.toInstant(TimeZone.currentSystemDefault())
            currentOrNextLesson.clear()
            currentOrNextLesson.addAll(day.lessons.first()
                .filter {
                    val lessonTime = it.getLessonTimeItem()
                    val start =
                        lessonTime.start.atDate(day.date).toInstant(TimeZone.of("Europe/Berlin"))
                    val end = lessonTime.end.atDate(day.date).toInstant(TimeZone.of("Europe/Berlin"))
                    contextZoned in start..end
                }
                .map { currentLesson ->
                    currentLesson to day.lessons.first()
                        .filter {
                            it.subject == currentLesson.subject
                                    && (it.teacherIds - currentLesson.teacherIds.toSet()).size != it.teacherIds.size
                                    && ((it.roomIds.orEmpty() - currentLesson.roomIds.orEmpty()
                                .toSet()).size != it.roomIds?.size || it.roomIds.orEmpty()
                                .isEmpty() || currentLesson.roomIds.orEmpty().isEmpty())
                                    && it.subjectInstanceId == currentLesson.subjectInstanceId
                                    && it.getLessonTimeItem().lessonNumber > currentLesson.getLessonTimeItem().lessonNumber
                        }.sortedBy { it.lessonTimeItem!!.lessonNumber }
                        .takeContinuousBy { it.lessonTimeItem!!.lessonNumber }
                })
        }
        if (currentOrNextLesson.isNotEmpty()) Column currentLessons@{
            SectionTitle(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = (if (currentOrNextLesson.any { it.first.lessonTimeItem!!.start <= contextTime.time }) "Aktuelle Stunde" else "Nächste Stunde") + (if (currentOrNextLesson.size > 1) "n" else "") + if (currentOrNextLesson.size > 1) "n" else "",
                subtitle = "${
                    currentOrNextLesson.map { it.first.lessonTimeItem!!.lessonNumber }.distinct().sorted()
                        .joinToString { "$it." }
                } Stunde"
            )

            if (currentOrNextLesson.all { it.first.subject == null }) {
                val nextActualLesson: Lesson? = null
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
                currentOrNextLesson.forEach { (currentLessons, followingConnected) ->
                    CurrentLessonCard(currentLessons, followingConnected, contextTime)
                }
            }
        }

        Column yourDay@{
            SectionTitle(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Dein Tag",
                subtitle = ""
            )
        }
    }
}