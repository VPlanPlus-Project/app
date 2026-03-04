package plus.vplan.app.ui.components.file

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.data.file.FileOperationProgress
import plus.vplan.app.core.model.File
import plus.vplan.app.domain.usecase.file.GetFileThumbnailUseCase
import plus.vplan.app.feature.homework.ui.components.create.RenameFileDialog
import plus.vplan.app.utils.toHumanSize
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.ellipsis_vertical
import vplanplus.composeapp.generated.resources.pencil
import vplanplus.composeapp.generated.resources.trash_2

/**
 * Enhanced file row component with thumbnail support and progress tracking.
 * 
 * This is the new version of FileRow that uses the new file infrastructure
 * with thumbnail generation and FileOperationProgress tracking.
 * 
 * @param file The file to display
 * @param getThumbnailUseCase Use case for retrieving file thumbnails
 * @param progress Current file operation progress (upload/download)
 * @param canEdit Whether the user can edit (rename/delete) the file
 * @param onFileClick Action when the file is clicked (typically to open it)
 * @param onRenameClick Action when rename is clicked, receives new name
 * @param onDeleteClick Action when delete is confirmed
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileRowWithThumbnail(
    file: File,
    getThumbnailUseCase: GetFileThumbnailUseCase,
    progress: FileOperationProgress = FileOperationProgress.Idle,
    canEdit: Boolean = true,
    onFileClick: () -> Unit,
    onRenameClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    var isDropdownOpen by remember { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var isRenameOpen by rememberSaveable { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onFileClick,
                    onLongClick = if (canEdit) {
                        { isDropdownOpen = true }
                    } else null
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or generic icon
            FileThumbnail(
                file = file,
                getThumbnailUseCase = getThumbnailUseCase,
                size = 48.dp,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(Modifier.width(12.dp))
            
            // File info
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
            }
            
            // Menu button
            if (canEdit) {
                IconButton(onClick = { isDropdownOpen = true }) {
                    Icon(
                        painter = painterResource(Res.drawable.ellipsis_vertical),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).padding(2.dp)
                    )
                }
            }
        }
        
        // Progress indicator
        FileProgressIndicator(
            progress = progress,
            modifier = Modifier.padding(top = 4.dp),
            showStatusText = false
        )
    }

    // Context menu
    DropdownMenu(
        expanded = isDropdownOpen,
        onDismissRequest = { isDropdownOpen = false }
    ) {
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
        DropdownMenuItem(
            text = { 
                Text(
                    text = "Löschen",
                    color = MaterialTheme.colorScheme.error
                ) 
            },
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
    }

    // Rename dialog
    if (isRenameOpen) {
        RenameFileDialog(
            originalFileName = file.name,
            onDismissRequest = { isRenameOpen = false },
            onRename = { newName ->
                onRenameClick(newName)
                isRenameOpen = false
            }
        )
    }

    // Delete confirmation dialog
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
                    onClick = { 
                        onDeleteClick()
                        showDeleteDialog = false
                    },
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
