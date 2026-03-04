package plus.vplan.app.domain.usecase.file

import plus.vplan.app.core.data.file.FileRepository
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response

/**
 * Use case for opening a file using the platform's default application.
 * 
 * @param fileRepository The new file repository from core/data
 */
class OpenFileUseCase(
    private val fileRepository: FileRepository
) {
    /**
     * Opens a file using the platform's native file opener (Android Intent, iOS QuickLook).
     * The file must be downloaded locally first.
     * 
     * @param file The file to open
     * @return Response indicating success or failure
     */
    suspend operator fun invoke(file: File): Response<Unit> {
        return fileRepository.openFile(file)
    }
}
