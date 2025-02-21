package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.repository.FileDownloadProgress
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.ui.common.AttachedFile

interface FileRepository: WebEntityRepository<File> {
    suspend fun upsert(file: File)
    fun cacheFile(file: File, schoolApiAccess: SchoolApiAccess): Flow<FileDownloadProgress>
    suspend fun uploadFile(
        vppId: VppId.Active,
        document: AttachedFile
    ): Response<Int>
    suspend fun getMinIdForLocalFile(): Int
    suspend fun setOfflineReady(file: File, isOfflineReady: Boolean)
    suspend fun renameFile(file: File, newName: String, vppId: VppId.Active? = null)
    suspend fun deleteFile(file: File, vppId: VppId.Active? = null): Response.Error?
}