package plus.vplan.app.feature.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.feature.home.ui.components.Greeting
import plus.vplan.app.feature.home.ui.components.HolidayScreen
import plus.vplan.app.feature.home.ui.components.PagerSwitcher
import plus.vplan.app.feature.home.ui.components.current_day.CurrentDayView
import plus.vplan.app.feature.home.ui.components.next_day.NextDayView
import plus.vplan.app.feature.main.MainScreen
import plus.vplan.app.feature.schulverwalter.domain.usecase.InitializeSchulverwalterReauthUseCase
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.BrowserIntent
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.key_round

@Composable
fun HomeScreen(
    navHostController: NavHostController,
    contentPadding: PaddingValues,
    homeViewModel: HomeViewModel
) {
    HomeContent(
        state = homeViewModel.state,
        contentPadding = contentPadding,
        onOpenSchoolSettings = remember { { navHostController.navigate(MainScreen.SchoolSettings(openIndiwareSettingsSchoolId = it)) } },
        onEvent = homeViewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeState,
    contentPadding: PaddingValues,
    onOpenSchoolSettings: (schoolId: Int) -> Unit,
    onEvent: (event: HomeEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    val initializeSchulverwalterReauthUseCase = koinInject<InitializeSchulverwalterReauthUseCase>()

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
                val vppId = (state.currentProfile as? Profile.StudentProfile)?.vppId?.collectAsResultingFlow()?.value
                Greeting(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    displayName = vppId?.name?.split(" ")?.first() ?: ""
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
                    if (state.currentDay.lessons.all { it.getLessonTimeItem().end < state.currentTime.time }) {
                        pagerState.animateScrollToPage(1)
                    }
                }

                val school = (state.currentProfile)?.getSchool()?.filterIsInstance<CacheState.Done<School>>()?.map { it.data }?.collectAsState(null)?.value

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    item schoolAccessInvalid@{
                        androidx.compose.animation.AnimatedVisibility(
                            visible = school is School.IndiwareSchool && !school.credentialsValid,
                            enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var displaySchool by remember { mutableStateOf<School?>(null) }
                            LaunchedEffect(school) {
                                if (school is School.IndiwareSchool && !school.credentialsValid) displaySchool = school
                            }
                            if (displaySchool != null) InfoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                title = "Schulzugangsdaten abgelaufen",
                                text = "Die Schulzugangsdaten für ${displaySchool!!.name} sind abgelaufen. Bitte aktualisiere sie, um weiterhin auf dem neuesten Stand zu bleiben.",
                                textColor = MaterialTheme.colorScheme.onErrorContainer,
                                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                shadow = true,
                                buttonAction1 = { onOpenSchoolSettings(displaySchool!!.id) },
                                buttonText1 = "Aktualisieren",
                                imageVector = Res.drawable.key_round
                            )
                        }
                    }
                    item {
                        val vppId = ((state.currentProfile as? Profile.StudentProfile)?.vppId?.collectAsResultingFlow()?.value as? VppId.Active)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = vppId?.schulverwalterConnection?.isValid == false,
                            enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InfoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                title = "beste.schule-Zugangsdaten abgelaufen",
                                text = "Die Verbindung zu beste.schule ist nicht mehr gültig. Bitte melde dich erneut mit beste.schule an.",
                                textColor = MaterialTheme.colorScheme.onErrorContainer,
                                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                shadow = true,
                                buttonAction1 = { scope.launch {
                                    if (vppId == null) return@launch
                                    val url = initializeSchulverwalterReauthUseCase(vppId) ?: return@launch
                                    BrowserIntent.openUrl(url)
                                } },
                                buttonText1 = "Aktualisieren",
                                imageVector = Res.drawable.key_round
                            )
                        }
                    }
                    item timetable@{
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                                    .thenIf(Modifier.padding(bottom = 56.dp)) { state.nextDay != null },
                                pageSize = PageSize.Fill,
                                beyondViewportPageCount = 0,
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
    }
}