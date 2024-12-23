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
import androidx.compose.ui.text.style.TextOverflow
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
            state.lessons.forEach { lesson ->
                Text("${lesson.lessonTime.lessonNumber}: ${lesson.subject} (${lesson.isSubjectChanged}) | ${lesson.rooms.joinToString { it.name }} (${lesson.isRoomChanged}) | ${lesson.teachers.joinToString { it.name }} (${lesson.isTeacherChanged}) :: ${lesson.defaultLesson?.id} (${lesson.defaultLesson?.subject})")
            }
        }
    }
}