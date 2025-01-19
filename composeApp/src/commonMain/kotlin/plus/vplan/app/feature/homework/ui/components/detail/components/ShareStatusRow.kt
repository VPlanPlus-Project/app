package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ShareStatusRow(
    canEdit: Boolean,
    onSelect: (isPublic: Boolean) -> Unit,
    isPublic: Boolean
) {
    MetadataRow(
        key = {
            Text(
                text = "Freigabe",
                style = tableNameStyle()
            )
        },
        value = {
            var isDropdownOpen by remember { mutableStateOf(false) }
            MetadataValueContainer(
                canEdit = canEdit,
                onClick = { isDropdownOpen = true }
            ) {
                Text(
                    text = if (isPublic) "Geteilt" else "Privat",
                    style = tableValueStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DropdownMenu(
                    expanded = isDropdownOpen,
                    onDismissRequest = { isDropdownOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Teilen") },
                        onClick = {
                            onSelect(true)
                            isDropdownOpen = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Privat") },
                        onClick = {
                            onSelect(false)
                            isDropdownOpen = false
                        }
                    )
                }
            }
        }
    )
}