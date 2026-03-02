package plus.vplan.app.core.data.file

import androidx.compose.ui.graphics.ImageBitmap
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.DbFile
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.network.besteschule.NetworkRequestUnsuccessfulException
import plus.vplan.app.network.besteschule.ResponseDataWrapper
import plus.vplan.app.network.vpp.GenericAuthenticationProvider
import kotlin.time.Clock

class FileRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase,
    private val genericAuthenticationProvider: GenericAuthenticationProvider,
    private val thumbnailGenerator: ThumbnailGenerator,
    private val fileOpener: FileOpener,
    private val getFileSystemPath: (relativePath: String) -> String
) : FileRepository {
    
    private val logger = Logger.withTag("FileRepositoryImpl")
    private val fileProgressMap = mutableMapOf<Int, MutableStateFlow<FileOperationProgress>>()
    private val baseUrl = "https://vplan.plus/api/app/file/v1"
    
    override fun getFileById(id: Int): Flow<File?> {
        return vppDatabase.fileDao.getById(id).map { it?.toModel() }
    }
    
    override fun getFilesByIds(ids: List<Int>): Flow<List<File>> {
        return vppDatabase.fileDao.getAll().map { allFiles ->
            allFiles.filter { it.id in ids }.map { it.toModel() }
        }
    }
    
    override fun observeFileProgress(fileId: Int): Flow<FileOperationProgress> {
        return fileProgressMap.getOrPut(fileId) {
            MutableStateFlow(FileOperationProgress.Idle)
        }
    }
    
    private suspend fun updateProgress(fileId: Int, progress: FileOperationProgress) {
        fileProgressMap.getOrPut(fileId) {
            MutableStateFlow(FileOperationProgress.Idle)
        }.value = progress
    }
    
    override suspend fun uploadFile(
        vppId: VppId.Active,
        platformFile: PlatformFile,
        onProgress: (Float) -> Unit
    ): Response<File> = withContext(Dispatchers.IO) {
        // Generate temporary file ID for local storage
        val localFileId = (vppDatabase.fileDao.getLocalMinId() ?: -1).coerceAtMost(-1) - 1
        
        try {
            updateProgress(localFileId, FileOperationProgress.Uploading(0f))
            
            // Read file bytes
            val fileBytes = platformFile.readBytes()
            val fileName = platformFile.name
            val fileSize = platformFile.size() ?: 0L
            
            // Detect MIME type from file extension
            val mimeType = detectMimeType(fileName)
            
            // Upload to server
            val response = httpClient.post {
                url(baseUrl)
                vppId.buildVppSchoolAuthentication().authentication(this)
                header("File-Name", fileName)
                header(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                header(HttpHeaders.ContentLength, fileSize.toString())
                setBody(ByteReadChannel(fileBytes))
                onUpload { bytesSentTotal, contentLength ->
                    val progress = if (contentLength != null) {
                        (bytesSentTotal.toFloat() / contentLength.toFloat())
                    } else 0f
                    onProgress(progress)
                    updateProgress(localFileId, FileOperationProgress.Uploading(progress))
                }
            }
            
            if (!response.status.isSuccess()) {
                val error = Response.Error.Other("Upload failed: ${response.status}")
                updateProgress(localFileId, FileOperationProgress.Error(error))
                throw NetworkRequestUnsuccessfulException(response)
            }
            
            val serverFileId = response.body<ResponseDataWrapper<Int>>().data
            
            // Store file locally
            val filePath = "files/$serverFileId"
            val fullPath = getFileSystemPath(filePath)
            writeFile(fullPath, fileBytes)
            
            // Create file record in database
            val file = File(
                id = serverFileId,
                name = fileName,
                size = fileSize,
                isOfflineReady = true,
                cachedAt = Clock.System.now(),
                thumbnailPath = null,
                mimeType = mimeType
            )
            
            vppDatabase.fileDao.upsert(DbFile(
                id = file.id,
                createdAt = Clock.System.now(),
                createdByVppId = vppId.id,
                fileName = file.name,
                size = file.size,
                isOfflineReady = true,
                cachedAt = file.cachedAt,
                thumbnailPath = null,
                mimeType = mimeType
            ))
            
            updateProgress(localFileId, FileOperationProgress.Success(file))
            Response.Success(file)
        } catch (e: NetworkRequestUnsuccessfulException) {
            logger.e(e) { "Network error uploading file" }
            val error = Response.Error.Other("Network error: ${e.message}")
            updateProgress(localFileId, FileOperationProgress.Error(error))
            error
        } catch (e: Exception) {
            logger.e(e) { "Error uploading file" }
            val error = Response.Error.Other(e.message ?: "Unknown error")
            updateProgress(localFileId, FileOperationProgress.Error(error))
            error
        }
    }
    
    override suspend fun downloadFile(
        file: File,
        schoolApiAccess: VppSchoolAuthentication
    ): Flow<FileOperationProgress> = channelFlow {
        try {
            send(FileOperationProgress.Downloading(0f))
            updateProgress(file.id, FileOperationProgress.Downloading(0f))
            
            val response = httpClient.get {
                url("$baseUrl/${file.id}/download")
                schoolApiAccess.authentication(this)
                onDownload { bytesSentTotal, contentLength ->
                    val progress = if (contentLength == null) 0f 
                        else (bytesSentTotal.toFloat() / contentLength.toFloat())
                    send(FileOperationProgress.Downloading(progress))
                    updateProgress(file.id, FileOperationProgress.Downloading(progress))
                }
            }
            
            if (!response.status.isSuccess()) {
                val error = Response.Error.Other("Download failed: ${response.status}")
                send(FileOperationProgress.Error(error))
                updateProgress(file.id, FileOperationProgress.Error(error))
                throw NetworkRequestUnsuccessfulException(response)
            }
            
            val data = response.bodyAsBytes()
            
            // Store file locally
            val filePath = "files/${file.id}"
            val fullPath = getFileSystemPath(filePath)
            writeFile(fullPath, data)
            
            // Mark as offline ready
            vppDatabase.fileDao.setOfflineReady(file.id, true)
            
            val updatedFile = file.copy(isOfflineReady = true)
            send(FileOperationProgress.Success(updatedFile))
            updateProgress(file.id, FileOperationProgress.Success(updatedFile))
        } catch (e: NetworkRequestUnsuccessfulException) {
            logger.e(e) { "Network error downloading file ${file.id}" }
            val error = Response.Error.Other("Network error: ${e.message}")
            send(FileOperationProgress.Error(error))
            updateProgress(file.id, FileOperationProgress.Error(error))
        } catch (e: Exception) {
            logger.e(e) { "Error downloading file ${file.id}" }
            val error = Response.Error.Other(e.message ?: "Unknown error")
            send(FileOperationProgress.Error(error))
            updateProgress(file.id, FileOperationProgress.Error(error))
        }
    }
    
    override suspend fun makeFileOfflineReady(
        file: File,
        schoolApiAccess: VppSchoolAuthentication
    ): Response<Unit> = withContext(Dispatchers.IO) {
        if (file.isOfflineReady) return@withContext Response.Success(Unit)
        
        downloadFile(file, schoolApiAccess).first().let { result ->
            when (result) {
                is FileOperationProgress.Success -> Response.Success(Unit)
                is FileOperationProgress.Error -> result.error
                else -> Response.Error.Other("Download did not complete")
            }
        }
    }
    
    override suspend fun renameFile(
        file: File,
        newName: String,
        vppId: VppId.Active?
    ): Response<Unit> = withContext(Dispatchers.IO) {
        val oldName = file.name
        
        // Update locally first
        vppDatabase.fileDao.updateName(file.id, newName)
        
        // If it's a local-only file or no vppId provided, we're done
        if (file.id < 0 || vppId == null) {
            return@withContext Response.Success(Unit)
        }
        
        // Update on server
        try {
            val response = httpClient.patch {
                url("$baseUrl/${file.id}")
                vppId.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(FileUpdateNameRequest(newName))
            }
            
            if (!response.status.isSuccess()) {
                // Rollback on error
                vppDatabase.fileDao.updateName(file.id, oldName)
                throw NetworkRequestUnsuccessfulException(response)
            }
            
            Response.Success(Unit)
        } catch (e: NetworkRequestUnsuccessfulException) {
            logger.e(e) { "Network error renaming file ${file.id}" }
            vppDatabase.fileDao.updateName(file.id, oldName)
            Response.Error.Other("Network error: ${e.message}")
        } catch (e: Exception) {
            logger.e(e) { "Error renaming file ${file.id}" }
            vppDatabase.fileDao.updateName(file.id, oldName)
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }
    
    override suspend fun deleteFile(
        file: File,
        vppId: VppId.Active?
    ): Response<Unit> = withContext(Dispatchers.IO) {
        // Delete from local storage
        val filePath = "files/${file.id}"
        val fullPath = getFileSystemPath(filePath)
        deleteFilePlatform(fullPath)
        
        // Delete thumbnail if exists
        file.thumbnailPath?.let { thumbnailPath ->
            val fullThumbnailPath = getFileSystemPath(thumbnailPath)
            deleteFilePlatform(fullThumbnailPath)
        }
        
        // If it's a local-only file or no vppId provided, just delete locally
        if (file.id < 0 || vppId == null) {
            vppDatabase.fileDao.deleteById(file.id)
            vppDatabase.fileDao.deleteHomeworkFileConnections(file.id)
            return@withContext Response.Success(Unit)
        }
        
        // Delete from server
        try {
            val response = httpClient.delete {
                url("$baseUrl/${file.id}")
                vppId.buildVppSchoolAuthentication().authentication(this)
            }
            
            if (!response.status.isSuccess()) {
                throw NetworkRequestUnsuccessfulException(response)
            }
            
            // Delete from database after successful server deletion
            vppDatabase.fileDao.deleteById(file.id)
            vppDatabase.fileDao.deleteHomeworkFileConnections(file.id)
            
            Response.Success(Unit)
        } catch (e: NetworkRequestUnsuccessfulException) {
            logger.e(e) { "Network error deleting file ${file.id}" }
            Response.Error.Other("Network error: ${e.message}")
        } catch (e: Exception) {
            logger.e(e) { "Error deleting file ${file.id}" }
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }
    
    override suspend fun openFile(file: File): Response<Unit> = withContext(Dispatchers.IO) {
        val filePath = "files/${file.id}"
        val fullPath = getFileSystemPath(filePath)
        
        // Check if file exists locally
        if (!fileExists(fullPath)) {
            return@withContext Response.Error.Other("File not available offline")
        }
        
        fileOpener.openFile(file, fullPath)
    }
    
    override suspend fun getThumbnail(file: File): ImageBitmap? = withContext(Dispatchers.IO) {
        // Check if thumbnail already exists
        file.thumbnailPath?.let { thumbnailPath ->
            val fullPath = getFileSystemPath(thumbnailPath)
            if (fileExists(fullPath)) {
                return@withContext loadThumbnailFromPath(fullPath)
            }
        }
        
        null
    }
    
    override suspend fun generateThumbnail(file: File): Response<ImageBitmap> = withContext(Dispatchers.IO) {
        try {
            updateProgress(file.id, FileOperationProgress.GeneratingThumbnail)
            
            // Check if file exists locally
            val filePath = "files/${file.id}"
            val fullPath = getFileSystemPath(filePath)
            
            if (!fileExists(fullPath)) {
                val error = Response.Error.Other("File not available offline")
                updateProgress(file.id, FileOperationProgress.Error(error))
                return@withContext error
            }
            
            // Generate thumbnail
            val thumbnail = thumbnailGenerator.generateThumbnail(file, fullPath)
                ?: return@withContext Response.Error.Other("Thumbnail generation not supported for this file type")
            
            // Save thumbnail to disk
            val thumbnailPath = "files/thumbnails/${file.id}.jpg"
            val fullThumbnailPath = getFileSystemPath(thumbnailPath)
            saveThumbnail(thumbnail, fullThumbnailPath)
            
            // Update database with thumbnail path
            vppDatabase.fileDao.updateThumbnailPath(file.id, thumbnailPath)
            
            val updatedFile = file.copy(thumbnailPath = thumbnailPath)
            updateProgress(file.id, FileOperationProgress.Success(updatedFile))
            
            Response.Success(thumbnail)
        } catch (e: Exception) {
            logger.e(e) { "Error generating thumbnail for file ${file.id}" }
            val error = Response.Error.Other(e.message ?: "Unknown error")
            updateProgress(file.id, FileOperationProgress.Error(error))
            error
        }
    }
    
    private fun detectMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
    
    // Platform-specific file operations - will be implemented as expect/actual
    private suspend fun writeFile(path: String, content: ByteArray) {
        // Implementation delegated to platform
        writeFilePlatform(path, content)
    }
    
    private suspend fun deleteFileLocal(path: String) {
        // Implementation delegated to platform
        deleteFilePlatform(path)
    }
    
    private suspend fun fileExists(path: String): Boolean {
        // Implementation delegated to platform
        return fileExistsPlatform(path)
    }
    
    private suspend fun loadThumbnailFromPath(path: String): ImageBitmap? {
        // Implementation delegated to platform
        return loadThumbnailPlatform(path)
    }
    
    private suspend fun saveThumbnail(thumbnail: ImageBitmap, path: String) {
        // Implementation delegated to platform
        saveThumbnailPlatform(thumbnail, path)
    }
}

// Platform-specific file operations
expect suspend fun writeFilePlatform(path: String, content: ByteArray)
expect suspend fun deleteFilePlatform(path: String)
expect suspend fun fileExistsPlatform(path: String): Boolean
expect suspend fun loadThumbnailPlatform(path: String): ImageBitmap?
expect suspend fun saveThumbnailPlatform(thumbnail: ImageBitmap, path: String)

@Serializable
data class FileUpdateNameRequest(
    @SerialName("file_name") val fileName: String,
)

@Serializable
data class ResponseDataWrapper<T>(
    val data: T
) {
    companion object {
        inline fun <reified T> fromJson(json: String): ResponseDataWrapper<T>? {
            return try {
                kotlinx.serialization.json.Json.decodeFromString<ResponseDataWrapper<T>>(json)
            } catch (e: Exception) {
                null
            }
        }
    }
}
