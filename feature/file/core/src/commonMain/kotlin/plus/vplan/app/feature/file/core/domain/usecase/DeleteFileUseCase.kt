package plus.vplan.app.feature.file.core.domain.usecase

import plus.vplan.app.core.data.file.FileRepository
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.VppId

/**
 * Use case for deleting a file from the VPP server and local storage.
 *
 * @param fileRepository The new file repository from core/data
 */
class DeleteFileUseCase(
    private val fileRepository: FileRepository
) {
    /**
     * Deletes a file from the VPP server and removes it from local storage.
     *
     * @param file The file to delete
     * @param vppId The VPP ID that owns the file (null for local-only files)
     * @return Response indicating success or failure
     */
    suspend operator fun invoke(
        file: File,
        vppId: VppId.Active?
    ): Response<Unit> {
        return fileRepository.deleteFile(file, vppId)
    }
}