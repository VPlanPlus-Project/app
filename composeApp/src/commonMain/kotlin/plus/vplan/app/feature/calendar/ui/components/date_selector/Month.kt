package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import plus.vplan.app.feature.calendar.ui.DateSelectorDay
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

val weekHeight = 64.dp

@Composable
fun Month(
    startDate: LocalDate,
    days: List<DateSelectorDay>,
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
                days = remember(days) { days.filter { it.date >= date && it.date - 7.days < date } },
                height = if (keepWeek == date) weekHeight else weekHeight * scrollProgress,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                scrollProgress = scrollProgress
            )
        }
    }
}