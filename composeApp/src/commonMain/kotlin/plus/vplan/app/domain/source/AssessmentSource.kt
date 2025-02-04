package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.repository.AssessmentRepository

class AssessmentSource(
    private val assessmentRepository: AssessmentRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<Assessment>>>()
    fun getById(id: Int): Flow<CacheState<Assessment>> {
        return cache.getOrPut(id) { assessmentRepository.getById(id, false) }
    }
}