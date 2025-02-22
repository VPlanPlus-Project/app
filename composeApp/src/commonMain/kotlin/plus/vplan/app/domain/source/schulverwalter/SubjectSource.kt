package plus.vplan.app.domain.source.schulverwalter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.Subject
import plus.vplan.app.domain.repository.schulverwalter.SubjectRepository

class SubjectSource(
    private val subjectRepository: SubjectRepository
) {
    private val cache = hashMapOf<Int, Flow<CacheState<Subject>>>()

    fun getById(id: Int): Flow<CacheState<Subject>> {
        return cache.getOrPut(id) { subjectRepository.getById(id, false) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheState<Subject>>> {
        return subjectRepository.getAllIds().flatMapLatest { ids ->
            if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(ids.map { getById(it) }) { it.toList() }
        }
    }
}