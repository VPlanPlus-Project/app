package plus.vplan.app.feature.home.ui.components.next_day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.collectAsLoadingState
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.feature.home.ui.HomeViewDay
import plus.vplan.app.feature.home.ui.components.DayInfoCard
import plus.vplan.app.feature.home.ui.components.FollowingLessons
import plus.vplan.app.feature.home.ui.components.SectionTitle

@Composable
fun NextDayView(day: HomeViewDay) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column {
            val weekState = day.day.weekId?.let { App.weekSource.getById(it).collectAsLoadingState(it) }
            SectionTitle(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Nächster Schultag",
                subtitle = day.day.date.format(LocalDate.Format {
                    dayOfWeek(DayOfWeekNames("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"))
                    chars(", ")
                    dayOfMonth()
                    chars(". ")
                    monthName(MonthNames("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"))
                    char(' ')
                    year()
                }) + "\n${(weekState?.value as? CacheState.Done)?.data?.weekType ?: "Unbekannte"}-Woche (KW ${(weekState?.value as? CacheState.Done)?.data?.calendarWeek}, SW ${(weekState?.value as? CacheState.Done)?.data?.weekIndex})\n" + (if (day.substitutionPlan == null) "Stundenplan" else "Vertretungsplan")
            )
            if (day.day.info != null) DayInfoCard(Modifier.padding(vertical = 4.dp), info = day.day.info)
        }

        Column lessons@{
            val lessonTimes by combine(day.substitutionPlan.orEmpty().ifEmpty { day.timetable }.map { it.lessonTime }.distinct().map { App.lessonTimeSource.getById(it).filterIsInstance<CacheState.Done<LessonTime>>().map { it.data } }) { it.toList() }.collectAsState(emptyList())
            if (lessonTimes.isEmpty()) return@lessons
            SectionTitle(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "Nächste Stunden",
                subtitle =
                    if (day.substitutionPlan.orEmpty().ifEmpty { day.timetable }.isEmpty()) "Keine Stunden"
                    else "${day.timetable.minOf { l -> lessonTimes.first { it.id == l.lessonTime }.start }} bis ${day.timetable.maxOf { l -> lessonTimes.first { it.id == l.lessonTime }.end }}"
            )
            FollowingLessons(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(start = 4.dp),
                showFirstGradient = false,
                paddingStart = 4.dp,
                date = day.day.date,
                lessons = day.substitutionPlan.orEmpty().ifEmpty() { day.timetable }.groupBy { l -> lessonTimes.first { it.id == l.lessonTime }.lessonNumber }
            )
        }
    }
}