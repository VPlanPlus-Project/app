package plus.vplan.app.core.data.file

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy
import plus.vplan.app.core.model.File

actual class ThumbnailGenerator {
    
    actual suspend fun generateThumbnail(file: File, filePath: String): ImageBitmap? {
        return try {
            when (FileType.fromFileName(file.name)) {
                FileType.IMAGE -> generateImageThumbnail(filePath)
                FileType.PDF -> null // TODO: PDF rendering for iOS requires complex CGContext work
                else -> null // Generic icons will be handled in UI layer
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    @OptIn(ExperimentalForeignApi::class)
    private fun generateImageThumbnail(filePath: String): ImageBitmap? {
        return try {
            val nsData = NSData.dataWithContentsOfFile(filePath) ?: return null
            val bytes = nsData.toByteArray()
            
            val image = Image.makeFromEncoded(bytes)
            val bitmap = Bitmap.makeFromImage(image)
            
            // For iOS, return the original image for now
            // TODO: Implement proper scaling for large images to reduce memory usage
            bitmap.asComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray {
        return ByteArray(length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), bytes, length)
            }
        }
    }
}
