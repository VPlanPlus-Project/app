package plus.vplan.app.data.repository.schulverwalter

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.repository.schulverwalter.SubjectRepository
import plus.vplan.app.domain.model.schulverwalter.Subject

class SubjectRepositoryImpl(
    private val vppDatabase: VppDatabase
) : SubjectRepository {
    private val logger = Logger.withTag("SubjectRepositoryImpl")
    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Subject>> {
        if (forceReload) logger.w { "Force reloading subjects is not supported" }
        return vppDatabase.subjectDao.getById(id).map {
            if (it == null) CacheState.NotExisting(id.toString())
            else CacheState.Done(it.toModel())
        }
    }

    override fun getAllIds(): Flow<List<Int>> = vppDatabase.subjectDao.getAll()
}