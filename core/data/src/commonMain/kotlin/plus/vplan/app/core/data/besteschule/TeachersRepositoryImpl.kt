package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.dao.besteschule.BesteschuleTeacherDao
import plus.vplan.app.core.model.besteschule.BesteSchuleTeacher

class TeachersRepositoryImpl(
    private val teacherDao: BesteschuleTeacherDao
): TeachersRepository {
    override fun getById(id: Int): Flow<BesteSchuleTeacher?> {
        return teacherDao.getTeacher(id).map { it?.toModel() }
    }

    override fun getAll(): Flow<List<BesteSchuleTeacher>> {
        return teacherDao.getAll().map { items -> items.map { it.toModel() } }
    }
}