package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.openFile
import plus.vplan.app.feature.homework.ui.components.create.RenameFileDialog
import plus.vplan.app.utils.toHumanSize
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.cloud_download
import vplanplus.composeapp.generated.resources.ellipsis_vertical
import vplanplus.composeapp.generated.resources.pencil
import vplanplus.composeapp.generated.resources.square_arrow_out_up_right
import vplanplus.composeapp.generated.resources.trash_2

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileRow(
    file: File,
    canEdit: Boolean,
    downloadProgress: Float?,
    onDownloadClick: () -> Unit,
    onRenameClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    var isDropdownOpen by remember { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = {
                    if (!file.isOfflineReady) {
                        onDownloadClick()
                        return@combinedClickable
                    }
                    openFile(file)
                },
                onLongClick = if (!canEdit) null else {
                    { isDropdownOpen = true }
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            if (file.preview != null) {
                Image(
                    bitmap = file.preview!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else if (file.isOfflineReady) {
                Icon(
                    painter = painterResource(Res.drawable.square_arrow_out_up_right),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.cloud_download),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = file.size.toHumanSize(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            AnimatedVisibility(
                visible = downloadProgress != null,
                enter = expandVertically(expandFrom = Alignment.CenterVertically),
                exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically)
            ) {
                LinearProgressIndicator(
                    progress = { downloadProgress ?: 0f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
        IconButton(onClick = { isDropdownOpen = true }) {
            Icon(
                painter = painterResource(Res.drawable.ellipsis_vertical),
                contentDescription = null,
                modifier = Modifier.size(24.dp).padding(2.dp)
            )
        }

        var isRenameOpen by rememberSaveable { mutableStateOf(false) }

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
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    showDeleteDialog = true
                    isDropdownOpen = false
                }
            )
            DropdownMenuItem(
                text = { Text("Umbenennen") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.pencil),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = {
                    isRenameOpen = true
                    isDropdownOpen = false
                }
            )
        }

        if (isRenameOpen) RenameFileDialog(
            originalFileName = file.name,
            onDismissRequest = { isRenameOpen = false },
            onRename = { onRenameClick(it) }
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
            title = { Text("Datei löschen") },
            text = {
                Text("Bist du sicher, dass du die Datei löschen möchtest? Dies kann nicht rückgängig gemacht werden.")
            },
            confirmButton = {
                TextButton(
                    onClick = { onDeleteClick(); showDeleteDialog = false },
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