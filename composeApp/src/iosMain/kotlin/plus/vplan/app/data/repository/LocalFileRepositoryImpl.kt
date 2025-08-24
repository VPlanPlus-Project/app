package plus.vplan.app.data.repository

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.posix.memcpy
import plus.vplan.app.domain.repository.LocalFileRepository

class LocalFileRepositoryImpl : LocalFileRepository {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun writeFile(path: String, content: ByteArray) {
        val fileManager = NSFileManager.defaultManager
        val documentDirectory = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        ) ?: throw IllegalStateException("Could not access document directory")

        val folder = path.split("/").filter { it != "." && it.isNotBlank() }.dropLast(1).joinToString("/")
        val name = path.split("/").last { it.isNotBlank() }

        val folderPath = documentDirectory.URLByAppendingPathComponent(folder.removePrefix("/"))?.path
            ?: throw IllegalStateException("Invalid folder path")

        if (!fileManager.fileExistsAtPath(folderPath)) {
            val success = fileManager.createDirectoryAtPath(folderPath, withIntermediateDirectories = true, attributes = null, error = null)
            if (!success) {
                throw IllegalStateException("Could not create directory: $folderPath")
            }
        }

        // Create the full file path
        val filePath = documentDirectory.URLByAppendingPathComponent("/files/$name")?.relativePath
            ?: throw IllegalStateException("Invalid file path")
        val nsData = content.toNSData()

        val success = nsData.writeToFile(filePath, false)

        println(if (success) "Succeed" else "Failed")
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun deleteFile(path: String) {
        val fileManager = NSFileManager.defaultManager
        val documentDirectory = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        ) ?: throw IllegalStateException("Could not access document directory")

        val folder = path.split("/").filter { it != "." && it.isNotBlank() }.dropLast(1).joinToString("/")
        val name = path.split("/").last { it.isNotBlank() }

        val folderPath = documentDirectory.URLByAppendingPathComponent(folder.removePrefix("/"))?.path
            ?: throw IllegalStateException("Invalid folder path")

        if (!fileManager.fileExistsAtPath(folderPath)) {
            val success = fileManager.createDirectoryAtPath(folderPath, withIntermediateDirectories = true, attributes = null, error = null)
            if (!success) {
                throw IllegalStateException("Could not create directory: $folderPath")
            }
        }

        // Create the full file path
        val filePath = documentDirectory.URLByAppendingPathComponent("/files/$name")?.relativePath
            ?: throw IllegalStateException("Invalid file path")

        fileManager.removeItemAtPath(filePath, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getFile(path: String): ByteArray? {
        val fileManager = NSFileManager.defaultManager
        val documentDirectory = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        ) ?: throw IllegalStateException("Could not access document directory")

        val folder = path.split("/").filter { it != "." && it.isNotBlank() }.dropLast(1).joinToString("/")
        val name = path.split("/").last { it.isNotBlank() }

        val folderPath = documentDirectory.URLByAppendingPathComponent(folder.removePrefix("/"))?.path
            ?: throw IllegalStateException("Invalid folder path")

        if (!fileManager.fileExistsAtPath(folderPath)) {
            val success = fileManager.createDirectoryAtPath(folderPath, withIntermediateDirectories = true, attributes = null, error = null)
            if (!success) {
                throw IllegalStateException("Could not create directory: $folderPath")
            }
        }

        // Create the full file path
        val filePath = documentDirectory.URLByAppendingPathComponent("/files/$name")?.relativePath
            ?: throw IllegalStateException("Invalid file path")

        val data = fileManager.contentsAtPath(filePath)
        return data?.toByteArray()
    }

    @OptIn(ExperimentalForeignApi::class)
    fun NSData.toByteArray(): ByteArray {
        return ByteArray(length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), bytes, length)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    fun ByteArray.toNSData(): NSData = memScoped {
        NSData.create(bytes = allocArrayOf(this@toNSData), length = this@toNSData.size.toULong())
    }
}