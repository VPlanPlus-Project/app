package plus.vplan.app.feature.assessment.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.common.usecase.GetCurrentProfileUseCase
import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.application.UpdateResult

class UpdateAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) {
    suspend operator fun invoke(id: Int): UpdateResult {
        val profile = getCurrentProfileUseCase().first() as? Profile.StudentProfile
            ?: return UpdateResult.ERROR
        
        val vppId = profile.vppId ?: return UpdateResult.ERROR

        val schoolApiAccess = vppId.buildVppSchoolAuthentication()
        
        // Sync the specific assessment from the server with forceReload=true
        val success = assessmentRepository.syncById(schoolApiAccess, id, forceReload = true)
        if (!success) return UpdateResult.DOES_NOT_EXIST
        
        assessmentRepository.getById(id).first()
            ?: return UpdateResult.DOES_NOT_EXIST
        
        return UpdateResult.SUCCESS
    }
}