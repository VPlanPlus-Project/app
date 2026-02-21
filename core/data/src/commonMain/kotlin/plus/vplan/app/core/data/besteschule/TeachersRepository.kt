package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.besteschule.BesteSchuleTeacher

interface TeachersRepository {
    fun getById(id: Int): Flow<BesteSchuleTeacher?>
    fun getAll(): Flow<List<BesteSchuleTeacher>>
}