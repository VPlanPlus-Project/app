package plus.vplan.app.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.SchoolDay
import plus.vplan.app.feature.home.ui.components.Greeting
import plus.vplan.app.feature.home.ui.components.PagerSwitcher
import plus.vplan.app.feature.home.ui.components.current_day.CurrentDayView
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.undraw_relaxing_at_home_black
import vplanplus.composeapp.generated.resources.undraw_relaxing_at_home_white

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel
) {
    HomeContent(
        state = homeViewModel.state,
        onEvent = homeViewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeState,
    onEvent: (event: HomeEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullToRefreshState,
        onRefresh = { onEvent(HomeEvent.OnRefresh) },
        isRefreshing = state.isUpdating,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize()) content@{
            Spacer(Modifier.height(WindowInsets.systemBars.asPaddingValues().calculateTopPadding()))
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                Greeting(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    profileName = state.currentProfile?.displayName ?: "",
                    time = remember(state.currentTime.hour) { state.currentTime.time }
                )
                Spacer(Modifier.height(4.dp))
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { 2 }
                )
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
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        when (page) {
                            0 -> {
                                when (state.currentDay) {
                                    is SchoolDay.NormalDay -> CurrentDayView(
                                        day = state.currentDay,
                                        contextTime = LocalDateTime(2025, 1, 6, 8, 0, 0)
                                    )
                                    is SchoolDay.Holiday -> {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Image(
                                                painter = painterResource(
                                                    if (!isSystemInDarkTheme()) Res.drawable.undraw_relaxing_at_home_white
                                                    else Res.drawable.undraw_relaxing_at_home_black
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxWidth(),
                                                contentScale = ContentScale.Inside
                                            )
                                            Column(Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = "Schulfreier Tag",
                                                    style = MaterialTheme.typography.headlineMedium.copy(
                                                        brush = Brush.horizontalGradient(
                                                            listOf(
                                                                MaterialTheme.colorScheme.tertiary,
                                                                MaterialTheme.colorScheme.primary
                                                            )
                                                        )
                                                    )
                                                )
                                                state.currentDay.nextRegularSchoolDay?.let { nextDay ->
                                                    Text(
                                                        text = "Nächster Schultag am ${nextDay.format(remember { LocalDate.Format {
                                                            dayOfWeek(DayOfWeekNames("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"))
                                                            chars(", ")
                                                            dayOfMonth()
                                                            chars(". ")
                                                            monthName(MonthNames("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"))
                                                            char(' ')
                                                            year()
                                                        } })}",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    is SchoolDay.Unknown -> Text("Unbekannter Tag")
                                    null -> Unit
                                }
                            }
                        }
                    }

                    if (state.currentDay?.nextRegularSchoolDay != null) PagerSwitcher(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        swipeProgress = pagerState.currentPage + pagerState.currentPageOffsetFraction,
                        nextDate = state.currentDay.nextRegularSchoolDay!!,
                        onSelectPage = { scope.launch { pagerState.animateScrollToPage(it) } }
                    )
                }
            }
        }
    }
}