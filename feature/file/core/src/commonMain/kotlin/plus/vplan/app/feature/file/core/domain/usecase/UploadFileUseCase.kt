package plus.vplan.app.feature.file.core.domain.usecase

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.data.file.FileOperationProgress
import plus.vplan.app.core.data.file.FileRepository
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.VppId

/**
 * Use case for uploading a file to VPP server with progress tracking.
 *
 * @param fileRepository The new file repository from core/data
 */
class UploadFileUseCase(
    private val fileRepository: FileRepository
) {
    /**
     * Uploads a file to the VPP server and returns the File object.
     *
     * @param vppId The VPP ID to upload the file for
     * @param platformFile The file to upload
     * @return Response containing the uploaded File object, or an error
     */
    suspend operator fun invoke(
        vppId: VppId.Active,
        platformFile: PlatformFile
    ): Response<File> {
        return fileRepository.uploadFile(vppId, platformFile)
    }

    /**
     * Observes the upload progress for a specific file.
     *
     * @param fileId The ID of the file being uploaded
     * @return Flow of FileOperationProgress states
     */
    fun observeProgress(fileId: Int): Flow<FileOperationProgress> {
        return fileRepository.observeFileProgress(fileId)
    }
}