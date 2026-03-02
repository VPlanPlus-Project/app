package plus.vplan.app.core.data.file

import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response

/**
 * Platform-specific file opener.
 * Opens files using the native system viewer (e.g., QuickLook on iOS, Intent on Android).
 */
expect class FileOpener {
    /**
     * Open the file in the system's default viewer.
     * 
     * @param file The file metadata
     * @param filePath The local file system path to the file content
     * @return Response.Success or Response.Error
     */
    suspend fun openFile(file: File, filePath: String): Response<Unit>
}
