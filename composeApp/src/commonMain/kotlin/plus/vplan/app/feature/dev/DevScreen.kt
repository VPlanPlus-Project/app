package plus.vplan.app.feature.dev

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DevScreen() {
    val viewModel = koinViewModel<DevViewModel>()
    val state = viewModel.state
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                onClick = { viewModel.onEvent(DevEvent.Refresh) }
            ) {
                Text("Refresh")
            }
            state.homework.forEach { homework ->
                Column(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(homework.defaultLesson?.subject ?: homework.group?.name ?: "wtf")
                        Text(homework.dueTo.toString())
                    }
                    Text(homework.tasks.joinToString("\n") { it.content })
                }
            }
        }
    }
}