package plus.vplan.app.domain.usecase.file

import plus.vplan.app.core.data.file.FileRepository
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.VppId

/**
 * Use case for renaming a file on the VPP server.
 * 
 * @param fileRepository The new file repository from core/data
 */
class RenameFileUseCase(
    private val fileRepository: FileRepository
) {
    /**
     * Renames a file on the VPP server.
     * 
     * @param file The file to rename
     * @param newName The new name for the file
     * @param vppId The VPP ID that owns the file (null for local-only files)
     * @return Response indicating success or failure
     */
    suspend operator fun invoke(
        file: File,
        newName: String,
        vppId: VppId.Active?
    ): Response<Unit> {
        return fileRepository.renameFile(file, newName, vppId)
    }
}
