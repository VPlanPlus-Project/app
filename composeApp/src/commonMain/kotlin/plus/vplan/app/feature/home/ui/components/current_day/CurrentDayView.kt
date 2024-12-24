package plus.vplan.app.feature.home.ui.components.current_day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
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
import plus.vplan.app.domain.model.SchoolDay
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.utils.takeContinuousBy
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.info

private val subtitleDateFormat = LocalDate.Format {
    dayOfWeek(DayOfWeekNames("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"))
    chars(", ")
    dayOfMonth(Padding.NONE)
    chars(". ")
    monthName(MonthNames("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"))
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
    day: SchoolDay.NormalDay,
    contextTime: LocalDateTime
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val currentLessons = remember("${contextTime.hour}:${contextTime.minute}") {
            val contextZoned = contextTime.toInstant(TimeZone.currentSystemDefault())
            day.lessons.filter {
                val start = it.lessonTime.start.atDate(day.date).toInstant(TimeZone.of("Europe/Berlin"))
                val end = it.lessonTime.end.atDate(day.date).toInstant(TimeZone.of("Europe/Berlin"))
                contextZoned in start..end
            }.map { currentLesson ->
                currentLesson to day.lessons.filter {
                    it.subject == currentLesson.subject
                            && (it.teachers - currentLesson.teachers.toSet()).size != it.teachers.size
                            && ((it.rooms.orEmpty() - currentLesson.rooms.orEmpty().toSet()).size != it.rooms?.size || it.rooms.orEmpty().isEmpty() || currentLesson.rooms.orEmpty().isEmpty())
                            && it.defaultLesson == currentLesson.defaultLesson
                            && it.lessonTime.lessonNumber > currentLesson.lessonTime.lessonNumber
                }.sortedBy { it.lessonTime.lessonNumber }.takeContinuousBy { it.lessonTime.lessonNumber }
            }
        }
        if (currentLessons.isNotEmpty()) Column currentLessons@{
            SectionTitle(
                title = "Aktuelle Stunden",
                subtitle = "${currentLessons.map { it.first.lessonTime.lessonNumber }.distinct().sorted().joinToString { "$it." } } Stunde"
            )

            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
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
                title = "Dein Tag",
                subtitle = "${day.date.format(remember { subtitleDateFormat })}, bis ${
                day.lessons.maxOf { it.lessonTime.end }.format(remember { subtitleTimeFormat })
            }")
            if (day.info != null) InfoCard(
                title = "Informationen deiner Schule",
                text = day.info,
                imageVector = Res.drawable.info,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SectionTitle(title)
        SectionSubtitle(subtitle)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun SectionSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.End
    )
}