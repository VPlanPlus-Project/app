package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.feature.homework.ui.components.DateSelectDrawer
import plus.vplan.app.feature.homework.ui.components.LessonSelectDrawer
import plus.vplan.app.ui.components.BackHandler
import plus.vplan.app.ui.components.Badge
import plus.vplan.app.ui.subjectIcon
import plus.vplan.app.utils.tryNoCatch
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.ellipsis_vertical
import vplanplus.composeapp.generated.resources.info
import vplanplus.composeapp.generated.resources.plus
import vplanplus.composeapp.generated.resources.rotate_cw
import vplanplus.composeapp.generated.resources.trash_2
import vplanplus.composeapp.generated.resources.x

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailPage(
    state: DetailState,
    onEvent: (event: DetailEvent) -> Unit
) {
    val homework = state.homework ?: return
    val profile = state.profile ?: return

    val localKeyboardController = LocalSoftwareKeyboardController.current

    var showLessonSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var showDateSelectDrawer by rememberSaveable { mutableStateOf(false) }

    var taskToEdit by rememberSaveable { mutableStateOf<Homework.HomeworkTask?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hausaufgabe",
                    style = MaterialTheme.typography.headlineLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f)
                )
                if (state.canEdit) {
                    FilledTonalIconButton(onClick = { onEvent(DetailEvent.DeleteHomework) }) {
                        AnimatedContent(
                            targetState = state.deleteState,
                        ) { deleteState ->
                            when (deleteState) {
                                UnoptimisticTaskState.InProgress -> CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp).padding(2.dp),
                                    strokeWidth = 2.dp
                                )
                                UnoptimisticTaskState.Error -> Icon(
                                    painter = painterResource(Res.drawable.info),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).padding(2.dp)
                                )
                                UnoptimisticTaskState.Success -> Icon(
                                    painter = painterResource(Res.drawable.check),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).padding(2.dp)
                                )
                                null -> Icon(
                                    painter = painterResource(Res.drawable.trash_2),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).padding(2.dp)
                                )
                            }
                        }
                    }
                }
                FilledTonalIconButton(
                    enabled = !state.isReloading,
                    onClick = { onEvent(DetailEvent.Reload) }
                ) {
                    AnimatedContent(
                        targetState = state.isReloading,
                    ) { isReloading ->
                        if (isReloading) CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(2.dp),
                            strokeWidth = 2.dp
                        )
                        else {
                            Icon(
                                painter = painterResource(Res.drawable.rotate_cw),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp).padding(2.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            val tableNameStyle = MaterialTheme.typography.bodyLarge.copy(Color.Gray)
            val tableValueStyle = MaterialTheme.typography.bodyMedium
            val tableCellModifier = Modifier.weight(1f, true)
            TableRow(
                key = {
                    Text(
                        text = "Klasse/Fach",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    val content: @Composable () -> Unit = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (homework.defaultLessonItem != null) {
                                Icon(
                                    painter = painterResource(homework.defaultLessonItem!!.subject.subjectIcon()),
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = homework.defaultLessonItem!!.subject,
                                    style = tableValueStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                Text(
                                    text = homework.groupItem!!.name,
                                    style = tableValueStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Box(tableCellModifier) {
                        if (state.canEdit) Box(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(state.canEdit) { showLessonSelectDrawer = true }
                                .padding(4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            content()
                        } else content()
                    }
                }
            )
            TableRow(
                key = {
                    Text(
                        text = "Fällig",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    val content: @Composable () -> Unit = {
                        Text(
                            text = homework.dueTo.toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Format {
                                dayOfMonth(Padding.ZERO)
                                char('.')
                                monthNumber(Padding.ZERO)
                                char('.')
                                year(Padding.ZERO)
                            }),
                            style = tableValueStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Box(tableCellModifier) {
                        if (state.canEdit) Box(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(state.canEdit) { showDateSelectDrawer = true }
                                .padding(4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            content()
                        } else content()
                    }
                }
            )
            if (homework is Homework.CloudHomework) {
                TableRow(
                    key = {
                        Text(
                            text = "Freigabe",
                            style = tableNameStyle,
                            modifier = tableCellModifier
                        )
                    },
                    value = {
                        val content: @Composable () -> Unit = {
                            Text(
                                text = if (homework.isPublic) "Geteilt" else "Privat",
                                style = tableValueStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Box(tableCellModifier) {
                            if (state.canEdit) {
                                var expanded by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable(state.canEdit) { expanded = true }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    content()
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Teilen") },
                                            onClick = {
                                                onEvent(DetailEvent.UpdateVisibility(true))
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Privat") },
                                            onClick = {
                                                onEvent(DetailEvent.UpdateVisibility(false))
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            } else content()
                        }
                    }
                )
            }
            if (state.canEdit) {
                Text(
                    text = "Tippe einen Wert an, um ihn zu bearbeiten",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }
            TableRow(
                key = {
                    Text(
                        text = "Status",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Box(tableCellModifier) {
                        Badge(
                            color = MaterialTheme.colorScheme.error,
                            text = "Überfällig"
                        )
                    }
                }
            )
            if (homework is Homework.CloudHomework) TableRow(
                key = {
                    Text(
                        text = "Erstellt von",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Text(
                        text = homework.createdByItem!!.name,
                        style = tableValueStyle,
                        modifier = tableCellModifier
                    )
                }
            )
            else TableRow(
                key = {
                    Text(
                        text = "Speicherort",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Text(
                        text = "Dieses Gerät",
                        style = tableValueStyle,
                        modifier = tableCellModifier
                    )
                }
            )
            TableRow(
                key = {
                    Text(
                        text = "Erstellt am",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Text(
                        text = homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Format {
                            dayOfMonth(Padding.ZERO)
                            char('.')
                            monthNumber(Padding.ZERO)
                            char('.')
                            year(Padding.ZERO)
                        }),
                        style = tableValueStyle,
                        modifier = tableCellModifier
                    )
                }
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            homework.getTasksFlow().collectAsState(emptyList()).value.forEach { task ->
                Logger.d { "Task ${task.id}, done: ${task.isDone(profile)}" }
                var isDropdownOpen by remember { mutableStateOf(false) }
                val isEditing = taskToEdit?.id == task.id
                var newContent by remember(task.id) { mutableStateOf(TextFieldValue(text = task.content, selection = TextRange(task.content.length))) }
                BackHandler(
                    enabled = isEditing,
                    onBack = { taskToEdit = null }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(
                            enabled = !isEditing,
                            onLongClick = if (!state.canEdit) null else {
                                { isDropdownOpen = true }
                            },
                            onClick = { onEvent(DetailEvent.ToggleTaskDone(task)) }
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
                                        onEvent(DetailEvent.UpdateTask(task, newContent.text))
                                        taskToEdit = null
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
                                targetState = state.taskDeleteState[task.id] == UnoptimisticTaskState.InProgress,
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
                                    checked = task.isDone(profile),
                                    onCheckedChange = { onEvent(DetailEvent.ToggleTaskDone(task)) }
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
                            targetState = taskToEdit == task,
                        ) { displayEdit ->
                            if (displayEdit) {
                                IconButton(
                                    onClick = { taskToEdit = null },
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.x),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp).padding(2.dp)
                                    )
                                }
                                return@AnimatedContent
                            }
                            if (state.canEdit) {
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
                            text = { Text("Bearbeiten") },
                            onClick = {
                                taskToEdit = task
                                isDropdownOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Löschen") },
                            onClick = {
                                onEvent(DetailEvent.DeleteTask(task))
                                isDropdownOpen = false
                            }
                        )
                    }
                }
            }
            if (state.canEdit) {
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
                        onClick = { onEvent(DetailEvent.AddTask(newTask)) },
                        enabled = newTask.isNotBlank() && state.newTaskState != UnoptimisticTaskState.InProgress
                    ) {
                        AnimatedContent(
                            targetState = state.newTaskState,
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
                    LaunchedEffect(state.newTaskState) { if (state.newTaskState == UnoptimisticTaskState.Success) cancel() }
                }
            }
        }
    }

    if (showLessonSelectDrawer) {
        LessonSelectDrawer(
            group = profile.groupItem!!,
            defaultLessons = profile.defaultLessonItems.filter { defaultLesson -> profile.defaultLessons.filterValues { !it }.none { it.key == defaultLesson.id } }.sortedBy { it.subject },
            selectedDefaultLesson = homework.defaultLessonItem,
            onSelectDefaultLesson = { onEvent(DetailEvent.UpdateDefaultLesson(it)) },
            onDismiss = { showLessonSelectDrawer = false }
        )
    }

    if (showDateSelectDrawer) {
        DateSelectDrawer(
            selectedDate = homework.dueTo.toLocalDateTime(TimeZone.currentSystemDefault()).date,
            onSelectDate = { onEvent(DetailEvent.UpdateDueTo(it)) },
            onDismiss = { showDateSelectDrawer = false }
        )
    }
}

@Composable
private fun TableRow(
    key: @Composable () -> Unit,
    value: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        key()
        value()
    }
}