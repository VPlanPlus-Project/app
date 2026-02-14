package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.besteschule.BesteSchuleTeacher

interface BesteSchuleTeachersRepository {
    suspend fun addTeachersToCache(items: List<BesteSchuleTeacher>)
    fun getTeacherFromCache(teacherId: Int): Flow<BesteSchuleTeacher?>
}