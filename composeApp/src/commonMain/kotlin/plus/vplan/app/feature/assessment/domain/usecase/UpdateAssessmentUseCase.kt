package plus.vplan.app.feature.assessment.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.repository.AssessmentRepository

class UpdateAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(id: Int): UpdateResult {
        return when(assessmentRepository.getById(id, true).first { it !is CacheState.Loading }) {
            is CacheState.Done -> UpdateResult.SUCCESS
            is CacheState.NotExisting -> UpdateResult.DOES_NOT_EXIST
            else -> UpdateResult.ERROR
        }
    }
}

enum class UpdateResult {
    SUCCESS, ERROR, DOES_NOT_EXIST
}