package plus.vplan.app.domain.usecase.file

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.data.file.FileOperationProgress
import plus.vplan.app.core.data.file.FileRepository
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.VppSchoolAuthentication

/**
 * Use case for downloading a file from VPP server with progress tracking.
 * 
 * @param fileRepository The new file repository from core/data
 */
class DownloadFileUseCase(
    private val fileRepository: FileRepository
) {
    /**
     * Downloads a file from the VPP server to local storage.
     * 
     * @param file The file to download
     * @param schoolApiAccess The authentication for accessing the file
     * @return Flow of FileOperationProgress states
     */
    suspend operator fun invoke(
        file: File,
        schoolApiAccess: VppSchoolAuthentication
    ): Flow<FileOperationProgress> {
        return fileRepository.downloadFile(file, schoolApiAccess)
    }

}
