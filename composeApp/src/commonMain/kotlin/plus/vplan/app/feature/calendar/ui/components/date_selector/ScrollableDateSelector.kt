package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

@Composable
fun ScrollableDateSelector(
    scrollProgress: Float,
    isScrollInProgress: Boolean,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    if (scrollProgress == 0f && !isScrollInProgress) WeekScroller(
        selectedDate = selectedDate,
        onChangeSelectedDate = onSelectDate
    )
//    if (scrollProgress in 0f..1f && isScrollInProgress) Month()
}