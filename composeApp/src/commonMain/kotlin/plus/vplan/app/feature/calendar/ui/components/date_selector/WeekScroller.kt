package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.until
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

const val WEEK_PAGER_SIZE = Int.MAX_VALUE

@Composable
fun WeekScroller(
    selectedDate: LocalDate,
    onChangeSelectedDate: (LocalDate) -> Unit
) {
    val referenceWeek = LocalDate.now().atStartOfWeek()
    val pagerState = rememberPagerState(initialPage = (WEEK_PAGER_SIZE / 2) + referenceWeek.until(selectedDate.atStartOfWeek(), DateTimeUnit.WEEK)) { WEEK_PAGER_SIZE }
    LaunchedEffect(pagerState.currentPage) {
        val date = (referenceWeek + ((pagerState.currentPage - WEEK_PAGER_SIZE / 2) * 7).days) + selectedDate.dayOfWeek.isoDayNumber.minus(1).days
        if (date.atStartOfWeek() != selectedDate.atStartOfWeek()) onChangeSelectedDate(date)
    }

    LaunchedEffect(selectedDate) {
        val currentlyOpenedWeek = referenceWeek.plus((pagerState.currentPage - WEEK_PAGER_SIZE / 2) * 7, DateTimeUnit.WEEK).atStartOfWeek()
        if (currentlyOpenedWeek.atStartOfWeek() != selectedDate.atStartOfWeek()) {
            val newPage = (WEEK_PAGER_SIZE / 2) + referenceWeek.until(selectedDate.atStartOfWeek(), DateTimeUnit.WEEK)
            pagerState.animateScrollToPage(newPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fill,
        beyondViewportPageCount = 2
    ) { page ->
        val startDate = referenceWeek + ((page - WEEK_PAGER_SIZE / 2) * 7).days
        Week(
            startDate = startDate,
            selectedDate = selectedDate,
            onDateSelected = onChangeSelectedDate,
            height = 56.dp
        )
    }
}