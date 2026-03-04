package plus.vplan.app.ui.components.file

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import plus.vplan.app.core.data.file.FileOperationProgress

/**
 * Displays progress for file operations (upload, download).
 * 
 * Shows a linear progress indicator with optional status text.
 * Automatically animates in/out when progress is active/idle.
 * 
 * @param progress The current file operation progress state
 * @param modifier Optional modifier for the container
 * @param showStatusText Whether to show status text (default true)
 */
@Composable
fun FileProgressIndicator(
    progress: FileOperationProgress,
    modifier: Modifier = Modifier,
    showStatusText: Boolean = true
) {
    val isActive = progress !is FileOperationProgress.Idle
    
    AnimatedVisibility(
        visible = isActive,
        enter = expandVertically(expandFrom = Alignment.CenterVertically),
        exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (progress) {
                is FileOperationProgress.Uploading -> {
                    if (showStatusText) {
                        Text(
                            text = "Wird hochgeladen... ${(progress.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
                is FileOperationProgress.Downloading -> {
                    if (showStatusText) {
                        Text(
                            text = "Wird heruntergeladen... ${(progress.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
                is FileOperationProgress.GeneratingThumbnail -> {
                    if (showStatusText) {
                        Text(
                            text = "Vorschau wird erstellt...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
                is FileOperationProgress.Success -> {
                    if (showStatusText) {
                        Text(
                            text = "Abgeschlossen",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is FileOperationProgress.Error -> {
                    if (showStatusText) {
                        Text(
                            text = "Fehler aufgetreten",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                FileOperationProgress.Idle -> {
                    // Should not be visible due to AnimatedVisibility
                }
            }
        }
    }
}
