package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.common.usecase.GetCurrentProfileUseCase
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.application.UpdateResult

class UpdateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) {
    suspend operator fun invoke(homeworkId: Int): UpdateResult {
        val profile = getCurrentProfileUseCase().first() as? Profile.StudentProfile
            ?: return UpdateResult.ERROR

        val vppId = profile.vppId ?: return UpdateResult.ERROR
        
        // Sync the specific homework from the server with forceReload=true
        val success = homeworkRepository.syncById(vppId, homeworkId, forceReload = true)
        if (success == HomeworkRepository.SyncResult.NotExists) return UpdateResult.DOES_NOT_EXIST
        if (success is HomeworkRepository.SyncResult.Error) return UpdateResult.ERROR

        homeworkRepository.getById(homeworkId).first()
            ?: return UpdateResult.DOES_NOT_EXIST
        
        return UpdateResult.SUCCESS
    }
}