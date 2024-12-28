package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import plus.vplan.app.utils.atStartOfMonth
import plus.vplan.app.utils.atStartOfWeek

@Composable
fun ScrollableDateSelector(
    scrollProgress: Float,
    allowInteractions: Boolean,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    if (scrollProgress == 0f && allowInteractions) WeekScroller(
        selectedDate = selectedDate,
        onChangeSelectedDate = onSelectDate
    )
    else if (scrollProgress in 0f..1f && !allowInteractions) Month(
        startDate = selectedDate.atStartOfMonth().atStartOfWeek(),
        selectedDate = selectedDate,
        keepWeek = selectedDate.atStartOfWeek(),
        scrollProgress = scrollProgress
    )
    else if (scrollProgress == 1f && allowInteractions) MonthScroller(
        selectedDate = selectedDate,
        onChangeSelectedDate = onSelectDate
    )
}