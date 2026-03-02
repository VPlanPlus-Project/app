package plus.vplan.app.core.data.file

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.temporaryDirectory
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response

/**
 * QuickLook integration for iOS file preview.
 * TODO: Implement proper QuickLook integration once dependency injection is set up
 */
actual class FileOpener {
    
    actual suspend fun openFile(file: File, filePath: String): Response<Unit> {
        // TODO: Implement QuickLook integration
        // This will require passing a platform-specific QuickLook handler through DI
        return Response.Error.Other("File opening not yet implemented for iOS")
    }
}
