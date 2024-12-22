package plus.vplan.app.feature.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize

@Composable
fun HomeScreen() {
    val viewModel = koinViewModel<HomeViewModel>()
    val state = viewModel.state

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) root@{
        Spacer(Modifier.height(WindowInsets.systemBars.asPaddingValues().calculateTopPadding()))
        Row {
            Button(
                text = "Update",
                size = ButtonSize.SMALL,
                onClick = { viewModel.onEvent(HomeUiEvent.Update) }
            )
            Button(
                text = "Sneak In",
                size = ButtonSize.SMALL,
                onClick = { viewModel.onEvent(HomeUiEvent.SneakIn) }
            )
            Button(
                text = "Delete",
                size = ButtonSize.SMALL,
                onClick = { viewModel.onEvent(HomeUiEvent.Delete(true)) }
            )
            Button(
                text = "Delete A",
                size = ButtonSize.SMALL,
                onClick = { viewModel.onEvent(HomeUiEvent.Delete(false)) }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Current Version: ${state.currentVersion}, class: ${state.group?.name}",
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
            val lessons = remember(state.lessons.size) { state.lessons.groupBy { it.date.dayOfWeek } }
            (1..5).map { DayOfWeek(it) }.forEach { dayOfWeek ->
                val lessonsForDay = lessons[dayOfWeek] ?: emptyList()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = dayOfWeek.name,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    style = MaterialTheme.typography.bodyMedium
                )
                lessonsForDay.forEach {
                    Text(
                        text = buildString {
                            append(it.lessonTime.lessonNumber)
                            append(": ")
                            append(it.subject)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}