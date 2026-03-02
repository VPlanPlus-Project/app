package plus.vplan.app.feature.assessment.domain.usecase

import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository

class DeleteFileUseCase(
    private val fileRepository: FileRepository,
    private val assessmentRepository: AssessmentRepository,
    private val localFileRepository: LocalFileRepository
) {
    suspend operator fun invoke(file: File, assessment: Assessment, profile: Profile.StudentProfile): Boolean {
        if (fileRepository.deleteFile(file, profile.vppId) != null) return false
        if (file.isOfflineReady) {
            localFileRepository.deleteFile("./files/" + file.id)
        }
        assessmentRepository.unlinkFile(profile.vppId as? VppId.Active, assessment.id, file.id)
        return true
    }
}