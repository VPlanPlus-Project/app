package plus.vplan.app.feature.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.home.ui.components.Greeting
import plus.vplan.app.feature.home.ui.components.HolidayScreen
import plus.vplan.app.feature.home.ui.components.PagerSwitcher
import plus.vplan.app.feature.home.ui.components.current_day.CurrentDayView
import plus.vplan.app.feature.home.ui.components.next_day.NextDayView

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    homeViewModel: HomeViewModel
) {
    HomeContent(
        state = homeViewModel.state,
        contentPadding = contentPadding,
        onEvent = homeViewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeState,
    contentPadding: PaddingValues,
    onEvent: (event: HomeEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullToRefreshState,
        onRefresh = { onEvent(HomeEvent.OnRefresh) },
        isRefreshing = state.isUpdating,
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            run greeting@{
                val vppId = (state.currentProfile as? Profile.StudentProfile)?.vppId?.let {
                    App.vppIdSource.getById(it).collectAsState(CacheState.Loading(it.toString()))
                }
                Greeting(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    displayName = vppId?.value.let {
                        if (it == null) return@let state.currentProfile?.name ?: ""
                        else return@let when (it) {
                            is CacheState.Done -> it.data.name
                            else -> ""
                        }
                    },
                    time = remember(state.currentTime.hour) { state.currentTime.time }
                )
            }
            Spacer(Modifier.height(4.dp))
            AnimatedContent(
                targetState = state.initDone
            ) { initDone ->
                if (!initDone) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@AnimatedContent
                }
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { 2 }
                )

                LaunchedEffect(state.nextDay) {
                    if (state.nextDay == null || state.currentDay == null) return@LaunchedEffect
                    if (state.nextDay.day.dayType != Day.DayType.REGULAR) return@LaunchedEffect
                    if (state.currentDay.timetable.all { it.getLessonTimeItem().end < state.currentTime.time }) {
                        pagerState.animateScrollToPage(1)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        pageSize = PageSize.Fill,
                        beyondViewportPageCount = 2,
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        val day = if (page == 0) state.currentDay else state.nextDay
                        when (day?.day?.dayType) {
                            Day.DayType.HOLIDAY, Day.DayType.WEEKEND -> HolidayScreen(isWeekend = day.day.dayType == Day.DayType.WEEKEND, nextRegularSchoolDay = day.day.nextSchoolDay?.split("/")?.let { LocalDate.parse(it[1]) })
                            Day.DayType.UNKNOWN -> Text("Unbekannter Tag")
                            Day.DayType.REGULAR -> {
                                if (page == 0) CurrentDayView(
                                    day = day,
                                    contextTime = state.currentTime
                                )
                                else NextDayView(day)
                            }
                            null -> Text("Nicht geladen")
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.nextDay != null,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        enter = slideInVertically(spring(stiffness = Spring.StiffnessHigh)) { it/2 } + fadeIn(),
                        exit = slideOutVertically(spring(stiffness = Spring.StiffnessHigh)) { it/2 } + fadeOut(),
                    ) {
                        val nextDayDate by remember { mutableStateOf(state.nextDay!!.day.date) }
                        PagerSwitcher(
                            modifier = Modifier.align(Alignment.Center).padding(bottom = 8.dp),
                            swipeProgress = pagerState.currentPage + pagerState.currentPageOffsetFraction,
                            nextDate = nextDayDate,
                            onSelectPage = { scope.launch { pagerState.animateScrollToPage(it) } }
                        )
                    }
                }
            }
        }
    }
}