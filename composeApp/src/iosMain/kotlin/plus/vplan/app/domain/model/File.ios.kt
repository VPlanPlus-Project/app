package plus.vplan.app.domain.model

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.temporaryDirectory
import plus.vplan.app.core.model.File
import plus.vplan.app.quicklook

interface OpenQuicklook {
    fun open(path: String)
}

@OptIn(ExperimentalForeignApi::class)
actual fun openFile(file: File) {
    val fileManager = NSFileManager.defaultManager
    val documentsDirectory = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        create = false,
        error = null,
        appropriateForURL = null
    )!!
    val fileURL = documentsDirectory.URLByAppendingPathComponent("files/${file.id}")!!

    val tempDirectory = fileManager.temporaryDirectory()
    val tempFile = tempDirectory.URLByAppendingPathComponent(file.name)!!

    fileManager.copyItemAtURL(srcURL = fileURL, toURL = tempFile, error = null)

    quicklook.open(tempFile.absoluteString!!)
}