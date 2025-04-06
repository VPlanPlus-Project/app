package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import co.touchlab.kermit.Logger
import kotlinx.datetime.LocalDate
import plus.vplan.app.feature.calendar.ui.DateSelectorDay
import plus.vplan.app.utils.atStartOfMonth
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.minus
import kotlin.time.Duration.Companion.days

@Composable
fun ScrollableDateSelector(
    days: List<DateSelectorDay>,
    containerMaxHeight: Dp,
    scrollProgress: Float,
    allowInteractions: Boolean,
    selectedDate: LocalDate,
    onSelectDate: (cause: DateSelectionCause, LocalDate) -> Unit
) {
    Logger.d { "ScrollProgress: $scrollProgress" }
    if (scrollProgress == 0f && allowInteractions) WeekScroller(
        selectedDate = selectedDate,
        scrollProgress = scrollProgress,
        days = days,
        onChangeSelectedDate = onSelectDate
    )
    else if (scrollProgress in 0f..2f && !allowInteractions) {
        val date = selectedDate.atStartOfMonth().atStartOfWeek()
        Month(
            startDate = date,
            days = remember(days) { days.filter { it.date >= date && it.date - 31.days < date } },
            selectedDate = selectedDate,
            keepWeek = selectedDate.atStartOfWeek(),
            containerMaxHeight = containerMaxHeight,
            scrollProgress = scrollProgress
        )
    }
    else if (scrollProgress in listOf(1f, 2f) && allowInteractions) MonthScroller(
        selectedDate = selectedDate,
        days = days,
        scrollProgress = scrollProgress,
        containerMaxHeight = containerMaxHeight,
        onChangeSelectedDate = onSelectDate
    )
}

enum class DateSelectionCause {
    IntervalScroll, DayClick
}