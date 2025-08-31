package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.until
import plus.vplan.app.feature.calendar.ui.DateSelectorDay
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

const val WEEK_PAGER_SIZE = Int.MAX_VALUE

@Composable
fun WeekScroller(
    selectedDate: LocalDate,
    days: List<DateSelectorDay>,
    scrollProgress: Float,
    onChangeSelectedDate: (DateSelectionCause, LocalDate) -> Unit
) {
    val referenceWeek = remember(LocalDate.now()) { LocalDate.now().atStartOfWeek() }
    val pagerState = rememberPagerState(initialPage = (WEEK_PAGER_SIZE / 2) + referenceWeek.until(selectedDate.atStartOfWeek(), DateTimeUnit.WEEK).toInt()) { WEEK_PAGER_SIZE }
    val isUserDragging = pagerState.interactionSource.collectIsDraggedAsState().value
    LaunchedEffect(pagerState.targetPage, isUserDragging) {
        if (isUserDragging) return@LaunchedEffect
        val date = (referenceWeek + ((pagerState.targetPage - WEEK_PAGER_SIZE / 2) * 7).days) + selectedDate.dayOfWeek.isoDayNumber.minus(1).days
        if (date.atStartOfWeek() != selectedDate.atStartOfWeek()) onChangeSelectedDate(DateSelectionCause.IntervalScroll, date)
    }

    LaunchedEffect(selectedDate) {
        val currentlyOpenedWeek = referenceWeek.plus((pagerState.currentPage - WEEK_PAGER_SIZE / 2) * 7, DateTimeUnit.WEEK).atStartOfWeek()
        if (currentlyOpenedWeek.atStartOfWeek() != selectedDate.atStartOfWeek()) {
            val newPage = (WEEK_PAGER_SIZE / 2) + referenceWeek.until(selectedDate.atStartOfWeek(), DateTimeUnit.WEEK).toInt()
            pagerState.animateScrollToPage(newPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fill,
        beyondViewportPageCount = 2
    ) { page ->
        val startDate = remember(page) { referenceWeek + ((page - WEEK_PAGER_SIZE / 2) * 7).days }
        Week(
            startDate = startDate,
            days = remember(days) { days.filter { it.date >= startDate && (it.date.minus(7.days)) < startDate } },
            selectedDate = selectedDate,
            onDateSelected = onChangeSelectedDate,
            height = weekHeightDefault,
            scrollProgress = scrollProgress
        )
    }
}