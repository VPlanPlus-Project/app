package plus.vplan.app.feature.home.ui.components.next_day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import plus.vplan.app.domain.model.SchoolDay
import plus.vplan.app.feature.home.ui.components.DayInfoCard
import plus.vplan.app.feature.home.ui.components.FollowingLessons
import plus.vplan.app.feature.home.ui.components.SectionTitle

@Composable
fun NextDayView(day: SchoolDay.NormalDay) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column {
            SectionTitle(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Nächster Schultag",
                subtitle = day.date.format(LocalDate.Format {
                    dayOfWeek(DayOfWeekNames("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"))
                    chars(", ")
                    dayOfMonth()
                    chars(". ")
                    monthName(MonthNames("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"))
                    char(' ')
                    year()
                }) + "\n${day.week.weekType}-Woche (KW ${day.week.calendarWeek}, SW ${day.week.weekIndex})"
            )
            if (day.info != null) DayInfoCard(Modifier.padding(vertical = 4.dp), info = day.info)
        }

        Column {
            SectionTitle(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Nächste Stunden",
                subtitle =
                    if (day.lessons.isEmpty()) "Keine Stunden"
                    else "${day.lessons.minOf { it.lessonTime.toValueOrNull()!!.start }} bis ${day.lessons.maxOf { it.lessonTime.toValueOrNull()!!.end }}"
            )
            FollowingLessons(
                showFirstGradient = false,
                date = day.date,
                lessons = day.lessons.groupBy { it.lessonTime.toValueOrNull()!!.lessonNumber })
        }
    }
}