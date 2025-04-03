package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import plus.vplan.app.feature.calendar.ui.CalendarDay
import plus.vplan.app.utils.atStartOfMonth
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.minus
import kotlin.time.Duration.Companion.days

@Composable
fun ScrollableDateSelector(
    days: List<CalendarDay>,
    scrollProgress: Float,
    allowInteractions: Boolean,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    if (scrollProgress == 0f && allowInteractions) WeekScroller(
        selectedDate = selectedDate,
        scrollProgress = scrollProgress,
        days = days,
        onChangeSelectedDate = onSelectDate
    )
    else if (scrollProgress in 0f..1f && !allowInteractions) {
        val date = selectedDate.atStartOfMonth().atStartOfWeek()
        Month(
            startDate = date,
            days = remember(days) { days.filter { it.day.date >= date && it.day.date - 31.days < date } },
            selectedDate = selectedDate,
            keepWeek = selectedDate.atStartOfWeek(),
            scrollProgress = scrollProgress
        )
    }
    else if (scrollProgress == 1f && allowInteractions) MonthScroller(
        selectedDate = selectedDate,
        days = days,
        onChangeSelectedDate = onSelectDate
    )
}