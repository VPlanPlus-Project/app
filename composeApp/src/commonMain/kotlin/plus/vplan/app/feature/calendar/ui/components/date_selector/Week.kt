package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate
import plus.vplan.app.feature.calendar.ui.DateSelectorDay
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

@Composable
fun Week(
    startDate: LocalDate,
    days: List<DateSelectorDay>,
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
            val day = remember(days) { days.firstOrNull { it.date == date } ?: DateSelectorDay(date) }
            Day(
                date = day.date,
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