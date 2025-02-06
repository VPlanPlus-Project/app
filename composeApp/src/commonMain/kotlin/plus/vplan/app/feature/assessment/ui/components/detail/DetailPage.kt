package plus.vplan.app.feature.assessment.ui.components.detail

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
import androidx.compose.material3.TextField
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
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.feature.assessment.ui.components.create.TypeDrawer
import plus.vplan.app.feature.assessment.ui.components.detail.components.TypeRow
import plus.vplan.app.feature.homework.ui.components.create.DateSelectDrawer
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState
import plus.vplan.app.feature.homework.ui.components.detail.components.CreatedAtRow
import plus.vplan.app.feature.homework.ui.components.detail.components.CreatedByRow
import plus.vplan.app.feature.homework.ui.components.detail.components.DueToRow
import plus.vplan.app.feature.homework.ui.components.detail.components.FileRow
import plus.vplan.app.feature.homework.ui.components.detail.components.SavedLocalRow
import plus.vplan.app.feature.homework.ui.components.detail.components.ShareStatusRow
import plus.vplan.app.feature.homework.ui.components.detail.components.SubjectGroupRow
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
    val assessment = state.assessment ?: return

    var showTypeSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var showDateSelectDrawer by rememberSaveable { mutableStateOf(false) }

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

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
                    text = "Leistungserhebung",
                    style = MaterialTheme.typography.headlineLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                    enabled = state.reloadingState != UnoptimisticTaskState.InProgress,
                    onClick = { onEvent(DetailEvent.Reload) }
                ) {
                    AnimatedContent(
                        targetState = state.reloadingState,
                    ) { reloadingState ->
                        when (reloadingState) {
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
                canEdit = false,
                allowGroup = false,
                defaultLesson = assessment.subjectInstanceItem,
                onClick = {}
            )
            TypeRow(
                canEdit = state.canEdit,
                type = assessment.type,
                onClick = { showTypeSelectDrawer = true }
            )
            DueToRow(
                canEdit = state.canEdit,
                isHomework = false,
                dueTo = assessment.date.atTime(LocalTime(0, 0)).toInstant(TimeZone.UTC),
                onClick = { showDateSelectDrawer = true },
            )
            if (assessment.creator is AppEntity.VppId) ShareStatusRow(
                canEdit = state.canEdit,
                isPublic = assessment.isPublic,
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
            if (assessment.creator is AppEntity.VppId) CreatedByRow(createdBy = assessment.createdByVppId!!)
            else SavedLocalRow()

            CreatedAtRow(createdAt = assessment.createdAt.toInstant(TimeZone.UTC))

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            if (state.canEdit) TextField(
                value = assessment.description,
                enabled = state.canEdit,
                onValueChange = {},
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            ) else Text(
                text = assessment.description
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                assessment.getFilesFlow().onEach { it.onEach { file -> if (file.isOfflineReady) file.getPreview() } }.collectAsState(emptyList()).value.forEach { file ->
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

    if (showDateSelectDrawer) {
        DateSelectDrawer(
            selectedDate = assessment.date,
            onSelectDate = { onEvent(DetailEvent.UpdateDate(it)) },
            onDismiss = { showDateSelectDrawer = false }
        )
    }

    if (showTypeSelectDrawer) TypeDrawer(
        selectedType = assessment.type,
        onSelectType = {},
        onDismiss = { showTypeSelectDrawer = false }
    )

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
            title = { Text("Leistungserhebung löschen") },
            text = {
                Text("Bist du sicher, dass du die Leistungserhebung löschen möchtest? Dies kann nicht rückgängig gemacht werden.")
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(DetailEvent.Delete) },
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