package plus.vplan.app.feature.calendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavHostController
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import kotlinx.datetime.until
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.feature.calendar.ui.components.date_selector.ScrollableDateSelector
import plus.vplan.app.feature.calendar.ui.components.date_selector.weekHeight
import plus.vplan.app.utils.now
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import kotlin.math.roundToInt

private const val CONTENT_PAGER_SIZE = Int.MAX_VALUE


@Composable
fun CalendarScreen(
    navHostController: NavHostController,
    viewModel: CalendarViewModel
) {
    val state = viewModel.state
    CalendarScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun CalendarScreenContent(
    state: CalendarState,
    onEvent: (event: CalendarEvent) -> Unit
) {
    val localDensity = LocalDensity.current

    var scrollProgress by remember { mutableStateOf(0f) }
    val contentScrollState = rememberScrollState()
    var isUserScrolling by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }
    LaunchedEffect(contentScrollState.isScrollInProgress) {
        if (!contentScrollState.isScrollInProgress) {
            scrollProgress = scrollProgress.roundToInt().toFloat()
            isAnimating = scrollProgress.roundToInt().toFloat() != scrollProgress
        }
        isUserScrolling = contentScrollState.isScrollInProgress
    }
    val animatedScrollProgress by animateFloatAsState(
        targetValue = scrollProgress,
        label = "scrollProgress",
        finishedListener = { isAnimating = false }
    )
    val displayScrollProgress = if (isUserScrolling) scrollProgress else animatedScrollProgress

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isContentAtTop = contentScrollState.value == 0
                val y = (with(localDensity) { available.y.toDp() }) / (5 * weekHeight)

                if ((isContentAtTop || scrollProgress > 0 && scrollProgress < 1) && available.y > 0) { // scroll to expand date picker
                    scrollProgress = (scrollProgress + y).coerceIn(0f, 1f)
                    return Offset(0f, available.y)
                }

                if (available.y < 0 && scrollProgress > 0) { // scroll to reduce date picker
                    scrollProgress = (scrollProgress + y).coerceIn(0f, 1f)
                    return Offset(0f, available.y)
                }

                return super.onPreScroll(available, source)
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .nestedScroll(scrollConnection)
        ) {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { isUserScrolling = true },
                            onDragEnd = { isUserScrolling = false; scrollProgress = scrollProgress.roundToInt().toFloat() },
                            onDragCancel = { isUserScrolling = false; scrollProgress = scrollProgress.roundToInt().toFloat() },
                        ) { _, dragAmount ->
                            val y = (with(localDensity) { dragAmount.toDp() }) / (5 * weekHeight)
                            scrollProgress = (scrollProgress + y).coerceIn(0f, 1f)
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    AnimatedContent(
                        targetState = state.selectedDate.format(LocalDate.Format {
                            monthName(MonthNames("Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"))
                            char(' ')
                            yearTwoDigits(2000)
                        }),
                    ) { displayDate ->
                        Text(
                            text = displayDate,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    IconButton(
                        onClick = { onEvent(CalendarEvent.SelectDate(LocalDate.now())) },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.calendar),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                ScrollableDateSelector(
                    scrollProgress = displayScrollProgress,
                    allowInteractions = !isUserScrolling && !isAnimating && displayScrollProgress.roundToInt().toFloat() == displayScrollProgress,
                    selectedDate = state.selectedDate,
                    onSelectDate = { onEvent(CalendarEvent.SelectDate(it)) }
                )
            }
            HorizontalDivider()
            val pagerState = rememberPagerState(initialPage = (CONTENT_PAGER_SIZE / 2) + LocalDate.now().until(state.selectedDate, DateTimeUnit.DAY)) { CONTENT_PAGER_SIZE }
            val isUserDragging = pagerState.interactionSource.collectIsDraggedAsState().value
            LaunchedEffect(pagerState.targetPage, isUserDragging) {
                if (isUserDragging) return@LaunchedEffect
                val date = LocalDate.now().plus((pagerState.targetPage - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                if (date != state.selectedDate) onEvent(CalendarEvent.SelectDate(date))
            }
            LaunchedEffect(state.selectedDate) {
                val currentlyOpenedDate = LocalDate.now().plus((pagerState.currentPage - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                if (currentlyOpenedDate != state.selectedDate) {
                    pagerState.animateScrollToPage((CONTENT_PAGER_SIZE / 2) + LocalDate.now().until(state.selectedDate, DateTimeUnit.DAY))
                }
            }
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fill,
                verticalAlignment = Alignment.Top,
                beyondViewportPageCount = 7,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScrollState)
            ) { page ->
                val date = LocalDate.now().plus((page - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                Column {
                    Text(date.toString())
                    repeat(3) {
                        Text("Content item $it", Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}