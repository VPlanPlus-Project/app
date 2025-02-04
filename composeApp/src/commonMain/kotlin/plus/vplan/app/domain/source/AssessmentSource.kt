package plus.vplan.app.domain.source

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheState<Assessment>>> {
        return assessmentRepository.getAll().map { it.map { item -> item.id } }.flatMapLatest {
            return@flatMapLatest if (it.isEmpty()) flowOf(emptyList())
            else combine(it.map { getById(it) }) { it.toList() }
        }
    }
}