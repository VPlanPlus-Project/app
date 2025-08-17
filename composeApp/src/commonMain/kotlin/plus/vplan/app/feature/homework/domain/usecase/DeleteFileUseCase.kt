package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.LocalFileRepository

class DeleteFileUseCase(
    private val fileRepository: FileRepository,
    private val homeworkRepository: HomeworkRepository,
    private val localFileRepository: LocalFileRepository
) {
    suspend operator fun invoke(file: File, homework: Homework, profile: Profile.StudentProfile): Boolean {
        if (fileRepository.deleteFile(file, profile.getVppIdItem()) != null) return false
        if (file.isOfflineReady) {
            localFileRepository.deleteFile("./files/" + file.id)
        }
        homeworkRepository.unlinkHomeworkFileLocally(homework, file.id)
        return true
    }
}