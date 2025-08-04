package plus.vplan.app.data.repository.schulverwalter

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.Teacher
import plus.vplan.app.domain.repository.schulverwalter.TeacherRepository

class TeacherRepositoryImpl(
    private val vppDatabase: VppDatabase
) : TeacherRepository {
    private val logger = Logger.withTag("TeacherRepositoryImpl")
    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Teacher>> {
        if (forceReload) logger.w { "Force reloading teachers is not supported" }
        return vppDatabase.schulverwalterTeacherDao.getById(id).map {
            if (it == null) CacheState.NotExisting(id.toString())
            else CacheState.Done(it.toModel())
        }
    }

    override fun getAllIds(): Flow<List<Int>> = vppDatabase.schulverwalterTeacherDao.getAll()
}