package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import co.touchlab.kermit.Logger
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.until
import plus.vplan.app.feature.calendar.ui.CalendarDay
import plus.vplan.app.utils.atStartOfMonth
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.now
import kotlin.time.Duration.Companion.days

const val MONTH_PAGER_SIZE = Int.MAX_VALUE

@Composable
fun MonthScroller(
    selectedDate: LocalDate,
    days: List<CalendarDay>,
    onChangeSelectedDate: (LocalDate) -> Unit
) {
    val referenceDate = LocalDate.now().atStartOfMonth()
    val pagerState = rememberPagerState(initialPage = (MONTH_PAGER_SIZE / 2) + referenceDate.until(selectedDate.atStartOfMonth(), DateTimeUnit.MONTH)) { MONTH_PAGER_SIZE }
    val isUserDragging = pagerState.interactionSource.collectIsDraggedAsState().value
    LaunchedEffect(pagerState.targetPage, isUserDragging) {
        if (isUserDragging) return@LaunchedEffect
        val date = referenceDate.plus((pagerState.targetPage - MONTH_PAGER_SIZE / 2), DateTimeUnit.MONTH).atStartOfMonth()
        if (date.month != selectedDate.month) onChangeSelectedDate(date)
    }

    LaunchedEffect(selectedDate) {
        val currentlyOpenedMonth = referenceDate.plus((pagerState.currentPage - MONTH_PAGER_SIZE / 2), DateTimeUnit.MONTH).atStartOfMonth()
        if (currentlyOpenedMonth.month != selectedDate.month) {
            Logger.d { "Switching to month ${selectedDate.month}" }
            val newPage = (MONTH_PAGER_SIZE / 2) + referenceDate.until(selectedDate.atStartOfMonth(), DateTimeUnit.MONTH)
            pagerState.animateScrollToPage(newPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fill,
        beyondViewportPageCount = 2
    ) { page ->
        val startDate = referenceDate.plus((page - MONTH_PAGER_SIZE / 2), DateTimeUnit.MONTH).atStartOfWeek()
        val days = days.filter { it.day.date >= startDate && it.day.date - 31.days < startDate }
        Month(
            startDate = startDate,
            days = days,
            selectedDate = selectedDate,
            keepWeek = selectedDate.atStartOfWeek(),
            scrollProgress = 1f,
            onDateSelected = onChangeSelectedDate
        )
    }
}