package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.LocalFileRepository

class DeleteFileUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val localFileRepository: LocalFileRepository
) {
    suspend operator fun invoke(file: File, homework: Homework, profile: Profile.StudentProfile): Boolean {
        if (file.isOfflineReady) {
            localFileRepository.deleteFile("./files/" + file.id)
        }
        homeworkRepository.unlinkHomeworkFile(
            vppId = profile.vppId?.getFirstValueOld() as? VppId.Active,
            homeworkId = homework.id,
            fileId = file.id
        )
        return true
    }
}