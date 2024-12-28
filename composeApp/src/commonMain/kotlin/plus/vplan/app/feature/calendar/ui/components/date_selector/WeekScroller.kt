package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

const val WEEK_PAGER_SIZE = 52

@Composable
fun WeekScroller() {
    val pagerState = rememberPagerState(initialPage = WEEK_PAGER_SIZE/2) { WEEK_PAGER_SIZE }
    val currentWeek = LocalDate.now().atStartOfWeek()
    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fill,
    ) { page ->
        val startDate = currentWeek + ((page - WEEK_PAGER_SIZE / 2) * 7).days
        Week(startDate, 56.dp)
    }
}