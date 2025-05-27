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
        val currentMaxId = vppDatabase.fcmDao.getMaxId().first() ?: -1
        val nextId = currentMaxId + 1
        vppDatabase.fcmDao.log(nextId, topic, message, timestamp)
    }

    override fun getAll(): Flow<List<DbFcmLog>> {
        return vppDatabase.fcmDao.getAll()
    }
}