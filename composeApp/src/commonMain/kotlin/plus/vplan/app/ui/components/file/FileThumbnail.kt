package plus.vplan.app.ui.components.file

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.File
import plus.vplan.app.domain.usecase.file.GetFileThumbnailUseCase
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.file_text

/**
 * Displays a thumbnail for a file, or a generic icon if no thumbnail is available.
 * 
 * For supported file types (images, PDFs), this will automatically generate and cache
 * a thumbnail. For other file types, it shows a generic file icon.
 * 
 * @param file The file to display a thumbnail for
 * @param getThumbnailUseCase The use case for retrieving/generating thumbnails
 * @param size The size of the thumbnail (default 48.dp)
 * @param modifier Optional modifier for the thumbnail container
 */
@Composable
fun FileThumbnail(
    file: File,
    getThumbnailUseCase: GetFileThumbnailUseCase,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    var thumbnail by remember(file.id) { mutableStateOf<ImageBitmap?>(null) }

    // Load thumbnail when file changes
    LaunchedEffect(file.id) {
        // Try to get cached thumbnail first
        thumbnail = getThumbnailUseCase(file)
        
        // If no cached thumbnail exists, try to generate one
        if (thumbnail == null && file.isOfflineReady) {
            getThumbnailUseCase.generate(file).let { response ->
                if (response is plus.vplan.app.core.model.Response.Success) {
                    thumbnail = response.data
                }
            }
        }
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when {
            thumbnail != null -> {
                // Show the actual thumbnail
                Image(
                    bitmap = thumbnail!!,
                    contentDescription = file.name,
                    modifier = Modifier.size(size),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                // Show generic file icon
                Box(
                    modifier = Modifier
                        .size(size)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.file_text),
                        contentDescription = file.name,
                        modifier = Modifier.size(size * 0.5f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
