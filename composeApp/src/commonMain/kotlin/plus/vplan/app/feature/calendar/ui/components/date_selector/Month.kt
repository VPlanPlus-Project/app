package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import plus.vplan.app.feature.calendar.ui.DateSelectorDay
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

@Composable
fun Month(
    startDate: LocalDate,
    days: List<DateSelectorDay>,
    selectedDate: LocalDate,
    containerMaxHeight: Dp,
    keepWeek: LocalDate,
    scrollProgress: Float,
    onDateSelected: (DateSelectionCause, LocalDate) -> Unit = { _, _ -> },
) {
    var weekRowHeight = remember(scrollProgress, containerMaxHeight) { if (scrollProgress <= 1f) weekHeightDefault * scrollProgress
    else (containerMaxHeight / 5) * (scrollProgress/2).coerceIn(0f, 1f) }

    Column(Modifier.fillMaxWidth()) {
        repeat(5) { i ->
            val date = remember(startDate, i) { startDate + (i * 7).days }
            val height = remember(date, keepWeek, scrollProgress, weekRowHeight) { if (keepWeek == date && scrollProgress <= 1f) weekHeightDefault else weekRowHeight }
            Box(Modifier.height(height)) {
                if (i > 0) HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .alpha((scrollProgress - 1).coerceIn(0f, 1f))
                )
                Week(
                    startDate = date,
                    days = remember(days) { days.filter { it.date >= date && it.date - 7.days < date } },
                    height = height,
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    scrollProgress = scrollProgress
                )
            }
        }
    }
}