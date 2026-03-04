package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.Homework
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState
import plus.vplan.app.ui.components.BackHandler
import plus.vplan.app.utils.tryNoCatch
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.ellipsis_vertical
import vplanplus.composeapp.generated.resources.pencil
import vplanplus.composeapp.generated.resources.trash_2
import vplanplus.composeapp.generated.resources.x

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    task: Homework.HomeworkTask,
    isDone: Boolean,
    taskDeleteState: UnoptimisticTaskState?,
    canEdit: Boolean,
    taskToEdit: Int?,
    onSetTaskToEdit: (Int?) -> Unit,
    onToggleTaskDone: () -> Unit,
    onUpdateTask: (String) -> Unit,
    onDeleteTask: () -> Unit,
) {
    var isDropdownOpen by remember { mutableStateOf(false) }
    val isEditing = taskToEdit == task.id
    var newContent by remember(task.id) { mutableStateOf(TextFieldValue(text = task.content, selection = TextRange(task.content.length))) }
    BackHandler(
        enabled = isEditing,
        onBack = { onSetTaskToEdit(null) }
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                enabled = !isEditing,
                onLongClick = if (!canEdit) null else {
                    { isDropdownOpen = true }
                },
                onClick = onToggleTaskDone
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) taskContent@{
            AnimatedContent(
                targetState = isEditing,
            ) { displayEdit ->
                if (displayEdit) {
                    IconButton(
                        onClick = {
                            onUpdateTask(newContent.text)
                            onSetTaskToEdit(null)
                        },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).padding(2.dp)
                        )
                    }
                    return@AnimatedContent
                }
                AnimatedContent(
                    targetState = taskDeleteState == UnoptimisticTaskState.InProgress,
                ) { isDeleting ->
                    if (isDeleting) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(2.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    else Checkbox(
                        checked = isDone,
                        onCheckedChange = { onToggleTaskDone() }
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            AnimatedContent(
                targetState = isEditing,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .padding(end = 8.dp)
            ) { displayEdit ->
                if (displayEdit) {
                    val focusRequester = remember { FocusRequester() }
                    TextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                        ),
                        placeholder = { Text(text = task.content) },
                    )
                    LaunchedEffect(Unit) { tryNoCatch { focusRequester.requestFocus() } }
                    return@AnimatedContent
                }
                Box(
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = task.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            AnimatedContent(
                targetState = taskToEdit == task.id,
            ) { displayEdit ->
                if (displayEdit) {
                    IconButton(
                        onClick = { onSetTaskToEdit(null) },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.x),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).padding(2.dp)
                        )
                    }
                    return@AnimatedContent
                }
                if (canEdit) {
                    IconButton(
                        onClick = { isDropdownOpen = true },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ellipsis_vertical),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).padding(2.dp)
                        )
                    }
                }
            }
        }
        DropdownMenu(
            expanded = isDropdownOpen,
            onDismissRequest = { isDropdownOpen = false }
        ) {
            DropdownMenuItem(
                text = { Text(
                    text = "Löschen",
                    color = MaterialTheme.colorScheme.error
                ) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.trash_2),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = {
                    onDeleteTask()
                    isDropdownOpen = false
                }
            )
            DropdownMenuItem(
                text = { Text("Bearbeiten") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.pencil),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = {
                    onSetTaskToEdit(task.id)
                    isDropdownOpen = false
                }
            )
        }
    }
}