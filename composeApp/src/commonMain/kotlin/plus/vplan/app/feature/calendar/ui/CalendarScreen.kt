package plus.vplan.app.feature.calendar.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import co.touchlab.kermit.Logger
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import plus.vplan.app.feature.calendar.ui.components.date_selector.ScrollableDateSelector
import plus.vplan.app.feature.calendar.ui.components.date_selector.weekHeight
import plus.vplan.app.utils.now
import kotlin.math.roundToInt

private val logger = Logger.withTag("CalendarScreen")

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
    LaunchedEffect(contentScrollState.isScrollInProgress) {
        isUserScrolling = contentScrollState.isScrollInProgress
        if (!isUserScrolling) scrollProgress = scrollProgress.roundToInt().toFloat()
    }
    val animatedScrollProgress by animateFloatAsState(targetValue = scrollProgress, label = "scrollProgress")
    val displayScrollProgress = if (isUserScrolling) scrollProgress else animatedScrollProgress

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isContentAtTop = contentScrollState.value == 0
                val y = (with(localDensity) { available.y.toDp() }) / (5 * weekHeight)
                logger.d { "Scrolled ${available.y}" }

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

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .nestedScroll(scrollConnection)
        ) {
            Box(
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
                    }
            ) {
//                Month(
//                    startDate = LocalDate(2024, 11, 25),
//                    keepWeek = LocalDate(2024, 12, 23),
//                    scrollProgress = displayScrollProgress
//                )
                ScrollableDateSelector(
                    scrollProgress = displayScrollProgress,
                    isScrollInProgress = isUserScrolling,
                    selectedDate = selectedDate,
                    onSelectDate = { selectedDate = it }
                )
            }
            HorizontalDivider()
            Text(
                text = selectedDate.format(LocalDate.Formats.ISO),
                modifier = Modifier.padding(8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScrollState)
            ) {
                repeat(100) {
                    Text("Content item $it", Modifier.padding(16.dp))
                }
            }
        }
    }
}