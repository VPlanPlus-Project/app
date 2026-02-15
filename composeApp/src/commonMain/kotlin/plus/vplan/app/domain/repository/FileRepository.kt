@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.repository.FileDownloadProgress
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.File
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.ui.common.AttachedFile
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface FileRepository: WebEntityRepository<File> {
    suspend fun upsertLocally(
        fileId: Int,
        fileName: String,
        fileSize: Long,
        isOfflineReady: Boolean,
        createdAt: Instant,
        createdBy: Int?
    )
    suspend fun upsert(file: File)
    fun downloadFileContent(file: File, schoolApiAccess: VppSchoolAuthentication): Flow<FileDownloadProgress>
    suspend fun uploadFile(
        vppId: VppId.Active,
        document: AttachedFile
    ): Response<Int>
    suspend fun getMinIdForLocalFile(): Int
    suspend fun setOfflineReady(file: File, isOfflineReady: Boolean)
    suspend fun renameFile(file: File, newName: String, vppId: VppId.Active? = null)
    suspend fun deleteFile(file: File, vppId: VppId.Active? = null): Response.Error?
}