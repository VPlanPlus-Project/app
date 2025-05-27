package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbFcmLog
import plus.vplan.app.domain.repository.FcmRepository

class FcmRepositoryImpl(
    private val vppDatabase: VppDatabase
) : FcmRepository {
    override suspend fun log(topic: String, message: String, timestamp: LocalDateTime) {
        vppDatabase.fcmDao.log(vppDatabase.fcmDao.getMaxId().first()?.plus(1) ?: 0, topic, message, timestamp)
    }

    override fun getAll(): Flow<List<DbFcmLog>> {
        return vppDatabase.fcmDao.getAll()
    }
}