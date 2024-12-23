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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import plus.vplan.app.feature.home.ui.components.Greeting

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel
) {
    HomeContent(
        state = homeViewModel.state
    )
}

@Composable
private fun HomeContent(
    state: HomeState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) root@{
        Spacer(Modifier.height(WindowInsets.systemBars.asPaddingValues().calculateTopPadding()))
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Greeting(state.currentProfile?.displayName ?: "", remember(state.currentTime.hour) { state.currentTime.time })
        }
    }
}