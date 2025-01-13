package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.TeacherRepository

class TeacherSource(
    private val teacherRepository: TeacherRepository
) {

    private val cache = hashMapOf<Int, Flow<CacheState<Teacher>>>()
    fun getById(id: Int): Flow<CacheState<Teacher>> {
        return cache.getOrPut(id) { teacherRepository.getById(id) }
    }
}