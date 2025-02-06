package plus.vplan.app.feature.assessment.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.repository.AssessmentRepository

class UpdateAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(id: Int) {
        assessmentRepository.getById(id, true).first { it !is CacheState.Loading }
    }
}