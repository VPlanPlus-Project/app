package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository

class DeleteFileUseCase(
    private val fileRepository: FileRepository,
    private val localFileRepository: LocalFileRepository
) {
    suspend operator fun invoke(file: File, profile: Profile.StudentProfile): Boolean {
        if (fileRepository.deleteFile(file, profile.getVppIdItem()) != null) return false
        if (file.isOfflineReady) {
            localFileRepository.deleteFile("./homework_files/" + file.id)
        }
        return true
    }
}