package plus.vplan.app.feature.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.domain.model.SchoolDay
import plus.vplan.app.feature.home.ui.components.Greeting
import plus.vplan.app.feature.home.ui.components.current_day.CurrentDayView

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
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullToRefreshState,
        onRefresh = { onEvent(HomeEvent.OnRefresh) },
        isRefreshing = state.isUpdating,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) content@{
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
                if (state.currentDay !is SchoolDay.NormalDay) return@content
                CurrentDayView(
                    day = state.currentDay,
                    contextTime = LocalDateTime(2025, 1, 6, 8, 0, 0)
                )
            }
        }
    }
}