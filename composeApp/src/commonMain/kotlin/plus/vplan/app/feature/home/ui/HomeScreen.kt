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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        Text("Hello")
        Row {
            Button(
                text = "Update",
                size = ButtonSize.SMALL,
                onClick = { viewModel.onEvent(HomeUiEvent.UpdateWeeks) }
            )
            Button(
                text = "Sneak In",
                size = ButtonSize.SMALL,
                onClick = { viewModel.onEvent(HomeUiEvent.SneakWeekIn) }
            )
            Button(
                text = "Delete",
                size = ButtonSize.SMALL,
                onClick = { viewModel.onEvent(HomeUiEvent.DeleteWeeks(true)) }
            )
            Button(
                text = "Delete A",
                size = ButtonSize.SMALL,
                onClick = { viewModel.onEvent(HomeUiEvent.DeleteWeeks(false)) }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            state.weeks.forEach {
                Text(
                    text = buildString {
                        append("KW")
                        append(it.calendarWeek)
                        append(": ")
                        append(it.start.toString())
                        append(" - ")
                        append(it.end.toString())
                        append(" (")
                        append(it.weekType)
                        append(" Woche, ")
                        append(it.weekIndex)
                        append(")")
                    },
                    maxLines = 1
                )
            }
        }
    }
}