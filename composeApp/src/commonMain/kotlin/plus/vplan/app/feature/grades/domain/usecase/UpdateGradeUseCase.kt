package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.repository.schulverwalter.GradeRepository
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult

class UpdateGradeUseCase(
    private val gradeRepository: GradeRepository
) {
    suspend operator fun invoke(gradeId: Int): UpdateResult {
        return when (gradeRepository.getById(gradeId, true).filter { it !is CacheState.Loading }.first()) {
            is CacheState.Done -> UpdateResult.SUCCESS
            else -> UpdateResult.ERROR
        }
    }
}