package plus.vplan.app.feature.file.core.domain.usecase

import androidx.compose.ui.graphics.ImageBitmap
import plus.vplan.app.core.data.file.FileRepository
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response

/**
 * Use case for getting a file thumbnail as an ImageBitmap.
 *
 * @param fileRepository The new file repository from core/data
 */
class GetFileThumbnailUseCase(
    private val fileRepository: FileRepository
) {
    /**
     * Gets the cached thumbnail for a file. Returns null if no thumbnail exists.
     * Use generateThumbnail() to create a thumbnail if one doesn't exist.
     *
     * @param file The file to get the thumbnail for
     * @return The thumbnail ImageBitmap, or null if no thumbnail exists
     */
    suspend operator fun invoke(file: File): ImageBitmap? {
        return fileRepository.getThumbnail(file)
    }

    /**
     * Generates a thumbnail for a file. The thumbnail will be generated if it doesn't
     * exist yet (for supported file types like images and PDFs).
     *
     * @param file The file to generate a thumbnail for
     * @return Response containing the thumbnail ImageBitmap, or an error
     */
    suspend fun generate(file: File): Response<ImageBitmap> {
        return fileRepository.generateThumbnail(file)
    }
}