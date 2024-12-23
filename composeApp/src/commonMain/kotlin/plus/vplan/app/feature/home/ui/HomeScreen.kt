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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import plus.vplan.app.domain.model.SchoolDay
import plus.vplan.app.feature.home.ui.components.Greeting
import plus.vplan.app.ui.components.InfoCard
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.info

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
            Spacer(Modifier.height(4.dp))
            Text(state.currentDay?.date?.toString() ?: "--")
            if (state.currentDay !is SchoolDay.NormalDay) return@root
            InfoCard(
                title = "Informationen für den Tag",
                text = state.currentDay.info ?: "Keine Informationen vorhanden",
                imageVector = Res.drawable.info,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            state.currentDay.lessons.forEach { lesson ->
                Text("${lesson.lessonTime.lessonNumber}: ${lesson.subject}")
            }
        }
    }
}