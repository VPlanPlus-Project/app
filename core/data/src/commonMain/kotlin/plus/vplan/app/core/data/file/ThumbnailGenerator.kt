package plus.vplan.app.core.data.file

import androidx.compose.ui.graphics.ImageBitmap
import plus.vplan.app.core.model.File

/**
 * Platform-specific thumbnail generator for files.
 * Generates 256x256dp thumbnails for supported file types (images, PDFs).
 */
expect class ThumbnailGenerator {
    /**
     * Generate a thumbnail for the given file.
     * 
     * @param file The file metadata
     * @param filePath The local file system path to the file content
     * @return ImageBitmap thumbnail or null if thumbnail generation is not supported for this file type
     */
    suspend fun generateThumbnail(file: File, filePath: String): ImageBitmap?
}

// Target thumbnail size in pixels
const val THUMBNAIL_SIZE = 256
