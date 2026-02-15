package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.model.File
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.FileRepository

class RenameFileUseCase(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(file: File, newName: String, profile: Profile.StudentProfile) {
        fileRepository.renameFile(file, newName, profile.vppId)
    }
}