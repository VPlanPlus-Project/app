package plus.vplan.app.core.data.file

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun writeFilePlatform(path: String, content: ByteArray) {
    val fileManager = NSFileManager.defaultManager
    
    // Create parent directories
    val parentPath = path.substringBeforeLast('/')
    if (!fileManager.fileExistsAtPath(parentPath)) {
        fileManager.createDirectoryAtPath(
            parentPath,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }
    
    // Write file
    val nsData = memScoped {
        NSData.create(bytes = allocArrayOf(content), length = content.size.toULong())
    }
    nsData.writeToFile(path, atomically = true)
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun deleteFilePlatform(path: String) {
    val fileManager = NSFileManager.defaultManager
    if (fileManager.fileExistsAtPath(path)) {
        fileManager.removeItemAtPath(path, error = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun fileExistsPlatform(path: String): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(path)
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun loadThumbnailPlatform(path: String): ImageBitmap? {
    return try {
        val nsData = NSData.dataWithContentsOfFile(path) ?: return null
        val bytes = nsData.toByteArray()
        val image = Image.makeFromEncoded(bytes)
        Bitmap.makeFromImage(image).asComposeImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun saveThumbnailPlatform(thumbnail: ImageBitmap, path: String) {
    val fileManager = NSFileManager.defaultManager
    
    // Create parent directories
    val parentPath = path.substringBeforeLast('/')
    if (!fileManager.fileExistsAtPath(parentPath)) {
        fileManager.createDirectoryAtPath(
            parentPath,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }
    
    // Convert ImageBitmap to Skia Bitmap
    // ImageBitmap from Compose can be directly converted using toComposeImageBitmap  
    // We need to encode it as JPEG
    val skiaBitmap = thumbnail.toSkiaBitmap()
    val image = Image.makeFromBitmap(skiaBitmap)
    val jpegData = image.encodeToData(EncodedImageFormat.JPEG, quality = 90)
        ?: throw IllegalStateException("Failed to encode thumbnail")
    
    val bytes = ByteArray(jpegData.size.toInt())
    jpegData.bytes.usePinned { pinnedBytes ->
        bytes.usePinned { pinnedTarget ->
            memcpy(pinnedTarget.addressOf(0), pinnedBytes.addressOf(0), jpegData.size.toULong())
        }
    }
    
    // Write to file
    val nsData = memScoped {
        NSData.create(bytes = allocArrayOf(bytes), length = bytes.size.toULong())
    }
    nsData.writeToFile(path, atomically = true)
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }
}

// Extension to convert Compose ImageBitmap to Skia Bitmap
// This is a workaround since we can't directly convert
private fun ImageBitmap.toSkiaBitmap(): Bitmap {
    // For now, return a simple bitmap
    // TODO: Find proper conversion method
    return Bitmap().apply {
        allocN32Pixels(width, height)
    }
}
