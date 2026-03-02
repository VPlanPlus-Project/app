package plus.vplan.app.core.data.file

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication

interface FileRepository {
    
    // Query operations
    fun getFileById(id: Int): Flow<File?>
    fun getFilesByIds(ids: List<Int>): Flow<List<File>>
    fun observeFileProgress(fileId: Int): Flow<FileOperationProgress>
    
    // Upload operations
    suspend fun uploadFile(
        vppId: VppId.Active,
        platformFile: PlatformFile,
        onProgress: (Float) -> Unit = {}
    ): Response<File>
    
    // Download operations
    suspend fun downloadFile(
        file: File,
        schoolApiAccess: VppSchoolAuthentication
    ): Flow<FileOperationProgress>
    
    suspend fun makeFileOfflineReady(
        file: File,
        schoolApiAccess: VppSchoolAuthentication
    ): Response<Unit>
    
    // File management
    suspend fun renameFile(
        file: File,
        newName: String,
        vppId: VppId.Active?
    ): Response<Unit>
    
    suspend fun deleteFile(
        file: File,
        vppId: VppId.Active?
    ): Response<Unit>
    
    // File opening
    suspend fun openFile(file: File): Response<Unit>
    
    // Thumbnail operations
    suspend fun getThumbnail(file: File): ImageBitmap?
    suspend fun generateThumbnail(file: File): Response<ImageBitmap>
}
