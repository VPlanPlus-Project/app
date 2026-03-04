package plus.vplan.app.core.data.file

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.temporaryDirectory
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response

/**
 * iOS-specific file opener implementation using QuickLook.
 * 
 * QuickLook is Apple's built-in document preview framework. This implementation:
 * 1. Copies the file to a temporary directory (QuickLook requirement)
 * 2. Triggers QuickLook preview through the OpenQuicklook interface
 * 
 * The OpenQuicklook interface is implemented in Swift and injected via Koin DI.
 */
interface OpenQuicklook {
    fun open(path: String)
}

actual class FileOpener(
    private val quicklook: OpenQuicklook
) {
    
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun openFile(file: File, filePath: String): Response<Unit> {
        return try {
            val fileManager = NSFileManager.defaultManager
            
            // Check if file exists
            if (!fileManager.fileExistsAtPath(filePath)) {
                return Response.Error.Other("File not found at $filePath")
            }
            
            // Create temp file for QuickLook
            val tempDirectory = fileManager.temporaryDirectory
            val tempFile = tempDirectory.URLByAppendingPathComponent(file.name)!!
            
            // Remove old temp file if it exists
            if (fileManager.fileExistsAtPath(tempFile.path!!)) {
                fileManager.removeItemAtURL(tempFile, error = null)
            }
            
            // Copy file to temp location
            val sourceURL = NSURL.fileURLWithPath(filePath)
            fileManager.copyItemAtURL(srcURL = sourceURL, toURL = tempFile, error = null)
            
            // Trigger QuickLook preview
            quicklook.open(tempFile.absoluteString!!)
            
            Response.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Error.Other(e.message ?: "Failed to open file")
        }
    }
}
