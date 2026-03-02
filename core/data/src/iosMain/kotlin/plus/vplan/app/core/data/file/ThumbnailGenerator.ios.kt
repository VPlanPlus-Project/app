package plus.vplan.app.core.data.file

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import plus.vplan.app.core.model.File
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

actual class ThumbnailGenerator {
    
    actual suspend fun generateThumbnail(file: File, filePath: String): ImageBitmap? {
        return try {
            when (FileType.fromFileName(file.name)) {
                FileType.IMAGE -> generateImageThumbnail(filePath)
                FileType.PDF -> null // PDF rendering not implemented for iOS yet
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
            
            // For iOS, we just return the original image
            // TODO: Implement proper scaling using UIImage or CIImage
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
