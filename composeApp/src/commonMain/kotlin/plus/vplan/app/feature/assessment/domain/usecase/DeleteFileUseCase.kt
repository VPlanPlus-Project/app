package plus.vplan.app.feature.assessment.domain.usecase

import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository

class DeleteFileUseCase(
    private val fileRepository: FileRepository,
    private val assessmentRepository: AssessmentRepository,
    private val localFileRepository: LocalFileRepository
) {
    suspend operator fun invoke(file: File, assessment: Assessment, profile: Profile.StudentProfile): Boolean {
        if (fileRepository.deleteFile(file, profile.getVppIdItem()) != null) return false
        if (file.isOfflineReady) {
            localFileRepository.deleteFile("./files/" + file.id)
        }
        assessmentRepository.unlinkFileFromAssessment(assessment.id, file.id)
        return true
    }
}