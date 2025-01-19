package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.plus
import vplanplus.composeapp.generated.resources.x

@Composable
fun NewTaskRow(
    newTaskState: UnoptimisticTaskState?,
    onAddTask: (String) -> Unit,
) {
    val localKeyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var newTask by remember { mutableStateOf("") }
        val cancel = {
            newTask = ""
            localKeyboardController?.hide()
            Unit
        }
        IconButton(
            onClick = { onAddTask(newTask) },
            enabled = newTask.isNotBlank() && newTaskState != UnoptimisticTaskState.InProgress
        ) {
            AnimatedContent(
                targetState = newTaskState,
            ) { newTaskState ->
                when (newTaskState) {
                    UnoptimisticTaskState.InProgress -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).padding(2.dp),
                        strokeWidth = 2.dp
                    )
                    else -> Icon(
                        painter = painterResource(Res.drawable.plus),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).padding(2.dp)
                    )
                }
            }
        }
        TextField(
            value = newTask,
            onValueChange = { newTask = it },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = MaterialTheme.colorScheme.outline,
            ),
            placeholder = { Text(text = "Weitere Aufgaben hinzufügen") },
        )
        AnimatedVisibility(
            visible = newTask.isNotBlank(),
            enter = expandHorizontally(),
            exit = shrinkHorizontally(),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            IconButton(
                onClick = cancel,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.x),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).padding(2.dp)
                )
            }
        }
        LaunchedEffect(newTaskState) { if (newTaskState == UnoptimisticTaskState.Success) cancel() }
    }
}