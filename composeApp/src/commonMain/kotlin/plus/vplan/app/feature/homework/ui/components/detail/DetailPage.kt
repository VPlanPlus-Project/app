package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.feature.homework.ui.components.create.DateSelectDrawer
import plus.vplan.app.feature.homework.ui.components.create.LessonSelectDrawer
import plus.vplan.app.feature.homework.ui.components.detail.components.CreatedAtRow
import plus.vplan.app.feature.homework.ui.components.detail.components.CreatedByRow
import plus.vplan.app.feature.homework.ui.components.detail.components.DueToRow
import plus.vplan.app.feature.homework.ui.components.detail.components.FileRow
import plus.vplan.app.feature.homework.ui.components.detail.components.NewTaskRow
import plus.vplan.app.feature.homework.ui.components.detail.components.SavedLocalRow
import plus.vplan.app.feature.homework.ui.components.detail.components.ShareStatusRow
import plus.vplan.app.feature.homework.ui.components.detail.components.StatusRow
import plus.vplan.app.feature.homework.ui.components.detail.components.SubjectGroupRow
import plus.vplan.app.feature.homework.ui.components.detail.components.TaskRow
import plus.vplan.app.ui.common.AttachedFile
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.ButtonType
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.file
import vplanplus.composeapp.generated.resources.image
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

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    var taskToEdit by rememberSaveable { mutableStateOf<Int?>(null) }

    val filePickerLauncher = rememberFilePickerLauncher(
        mode = PickerMode.Multiple(),
        type = PickerType.File()
    ) { files ->
        // Handle picked files
        Logger.d { "Picked files: ${files?.map { it.path }}" }
        files?.forEach { file ->
            onEvent(DetailEvent.AddFile(AttachedFile.Other(file)))
        }
    }

    val imagePickerLauncher = rememberFilePickerLauncher(
        mode = PickerMode.Multiple(),
        type = PickerType.Image
    ) { images ->
        Logger.d { "Picked images: ${images?.map { it.path }}" }
        images?.forEach { image ->
            onEvent(DetailEvent.AddFile(AttachedFile.Other(image)))
        }
    }

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
                    FilledTonalIconButton(onClick = { showDeleteDialog = true }) {
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
            SubjectGroupRow(
                canEdit = state.canEdit,
                defaultLesson = homework.defaultLessonItem,
                group = homework.groupItem,
                onClick = { showLessonSelectDrawer = true },
            )
            DueToRow(
                canEdit = state.canEdit,
                dueTo = homework.dueTo,
                onClick = { showDateSelectDrawer = true },
            )
            if (homework is Homework.CloudHomework) ShareStatusRow(
                canEdit = state.canEdit,
                isPublic = homework.isPublic,
                onSelect = { isPublic -> onEvent(DetailEvent.UpdateVisibility(isPublic)) }
            )

            if (state.canEdit) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Tippe einen Wert an, um ihn zu bearbeiten",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    HorizontalDivider()
                }
            }
            StatusRow(status = homework.getStatusFlow(state.profile).collectAsState(null).value)
            if (homework is Homework.CloudHomework) CreatedByRow(createdBy = homework.createdByItem!!)
            else SavedLocalRow()

            CreatedAtRow(createdAt = homework.createdAt)

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            homework.getTasksFlow().collectAsState(emptyList()).value.forEach { task ->
                TaskRow(
                    task = task,
                    isDone = task.isDone(state.profile),
                    taskDeleteState = state.taskDeleteState[task.id],
                    canEdit = state.canEdit,
                    taskToEdit = taskToEdit,
                    onSetTaskToEdit = { taskToEdit = it },
                    onToggleTaskDone = { onEvent(DetailEvent.ToggleTaskDone(task)) },
                    onUpdateTask = { onEvent(DetailEvent.UpdateTask(task, it)) },
                    onDeleteTask = {
                        if (homework.tasks.size == 1) showDeleteDialog = true
                        else onEvent(DetailEvent.DeleteTask(task))
                    }
                )
            }
            if (state.canEdit) NewTaskRow(
                newTaskState = state.newTaskState,
                onAddTask = { onEvent(DetailEvent.AddTask(it)) }
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                homework.getFilesFlow().onEach { it.onEach { file -> if (file.isOfflineReady) file.getPreview() } }.collectAsState(emptyList()).value.forEach { file ->
                    FileRow(
                        file = file,
                        canEdit = state.canEdit,
                        downloadProgress = state.fileDownloadState[file.id],
                        onDownloadClick = { onEvent(DetailEvent.DownloadFile(file)) },
                        onRenameClick = { newName -> onEvent(DetailEvent.RenameFile(file, newName)) },
                        onDeleteClick = { onEvent(DetailEvent.DeleteFile(file)) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.weight(1f).height(64.dp),
                    text = "Aus Dateien hinzufügen",
                    icon = Res.drawable.file,
                    state = ButtonState.Enabled,
                    size = ButtonSize.Small,
                    type = ButtonType.Secondary,
                    onClick = { filePickerLauncher.launch() }
                )
                Button(
                    modifier = Modifier.weight(1f).height(64.dp),
                    text = "Aus Galerie hinzufügen",
                    icon = Res.drawable.image,
                    state = ButtonState.Enabled,
                    size = ButtonSize.Small,
                    type = ButtonType.Secondary,
                    onClick = { imagePickerLauncher.launch() }
                )
            }

            Spacer(Modifier.height(WindowInsets.safeContent.asPaddingValues().calculateBottomPadding()))
        }
    }

    if (showLessonSelectDrawer) {
        LessonSelectDrawer(
            group = profile.groupItem!!,
            allowGroup = true,
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.trash_2),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            title = { Text("Hausaufgabe löschen") },
            text = {
                Text("Bist du sicher, dass du die Hausaufgabe löschen möchtest? Dies kann nicht rückgängig gemacht werden.")
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(DetailEvent.DeleteHomework) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}