package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate
import plus.vplan.app.feature.calendar.ui.CalendarDay
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

@Composable
fun Week(
    startDate: LocalDate,
    days: List<CalendarDay>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit = {},
    height: Dp,
    scrollProgress: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    ) {
        repeat(7) {
            val date = startDate + it.days
            val day = days.firstOrNull { it.day.date == date } ?: CalendarDay(date)
            Day(
                date = day.day.date,
                selectedDate = selectedDate,
                onClick = { onDateSelected(date) },
                height = height,
                isOtherMonth = selectedDate.month != date.month,
                scrollProgress = scrollProgress,
                homework = day.homework,
                assessments = day.assessments
            )
        }
    }
}