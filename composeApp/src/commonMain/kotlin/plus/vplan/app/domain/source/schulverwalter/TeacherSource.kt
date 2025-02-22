package plus.vplan.app.domain.source.schulverwalter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.Teacher
import plus.vplan.app.domain.repository.schulverwalter.TeacherRepository

class TeacherSource(
    private val teacherRepository: TeacherRepository
) {

    private val cache = hashMapOf<Int, Flow<CacheState<Teacher>>>()

    fun getById(id: Int): Flow<CacheState<Teacher>> {
        return cache.getOrPut(id) { teacherRepository.getById(id, false) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(): Flow<List<CacheState<Teacher>>> {
        return teacherRepository.getAllIds().flatMapLatest { ids ->
            if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(ids.map { getById(it) }) { it.toList() }
        }
    }
}