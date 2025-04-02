package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import plus.vplan.app.feature.calendar.ui.CalendarDay
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

val weekHeight = 64.dp

@Composable
fun Month(
    startDate: LocalDate,
    days: List<CalendarDay>,
    selectedDate: LocalDate,
    keepWeek: LocalDate,
    scrollProgress: Float,
    onDateSelected: (LocalDate) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        repeat(5) {
            val date = startDate + (it*7).days
            Week(
                startDate = date,
                days = days.filter { it.day.date >= date && it.day.date - 7.days < date },
                height = if (keepWeek == date) weekHeight else weekHeight * scrollProgress,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                scrollProgress = scrollProgress
            )
        }
    }
}