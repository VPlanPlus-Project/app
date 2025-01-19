package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import plus.vplan.app.ui.components.Badge
import plus.vplan.app.ui.subjectIcon
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.info
import vplanplus.composeapp.generated.resources.rotate_cw
import vplanplus.composeapp.generated.resources.trash_2

@Composable
fun DetailPage(
    state: DetailState,
    onEvent: (event: DetailEvent) -> Unit
) {
    val homework = state.homework ?: return
    val profile = state.profile ?: return

    var showLessonSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var showDateSelectDrawer by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
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
                                DeleteHomeworkDialogState.Deleting -> CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp).padding(2.dp),
                                    strokeWidth = 2.dp
                                )
                                DeleteHomeworkDialogState.Error -> Icon(
                                    painter = painterResource(Res.drawable.info),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).padding(2.dp)
                                )
                                DeleteHomeworkDialogState.Success -> Icon(
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
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            homework.getTasksFlow().collectAsState(emptyList()).value.forEach { task ->
                Logger.d { "Task ${task.id}, done: ${task.isDone(profile)}" }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEvent(DetailEvent.ToggleTaskDone(task)) }
                        .padding(end = 8.dp),
                ) {
                    Checkbox(
                        checked = task.isDone(profile),
                        onCheckedChange = { onEvent(DetailEvent.ToggleTaskDone(task)) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = task.content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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