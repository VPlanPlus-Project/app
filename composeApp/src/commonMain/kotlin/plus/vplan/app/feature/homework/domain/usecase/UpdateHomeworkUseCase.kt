package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult

class UpdateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val fileRepository: FileRepository,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) {
    suspend operator fun invoke(homeworkId: Int): UpdateResult {
        val profile = getCurrentProfileUseCase().first() as? Profile.StudentProfile
            ?: return UpdateResult.ERROR
        
        val vppId = profile.vppId as? VppId.Active
            ?: return UpdateResult.ERROR
        
        // Sync the specific homework from the server with forceReload=true
        val success = homeworkRepository.syncById(vppId, homeworkId, forceReload = true)
        if (!success) return UpdateResult.DOES_NOT_EXIST
        
        val homework = homeworkRepository.getById(homeworkId).first()
            ?: return UpdateResult.DOES_NOT_EXIST
        
        // Reload all files
        homework.files.forEach { file ->
            fileRepository.getById(file.id, forceReload = true).first()
        }
        
        return UpdateResult.SUCCESS
    }
}