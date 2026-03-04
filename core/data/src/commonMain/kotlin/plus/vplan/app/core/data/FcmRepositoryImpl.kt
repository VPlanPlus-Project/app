package plus.vplan.app.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.core.database.dao.FcmDao
import plus.vplan.app.core.database.model.database.DbFcmLog

class FcmRepositoryImpl(
    private val fcmDao: FcmDao,
) : FcmRepository {
    override suspend fun log(topic: String, message: String, timestamp: LocalDateTime) {
        val currentMaxId = fcmDao.getMaxId().first() ?: -1
        val nextId = currentMaxId + 1
        fcmDao.log(nextId, topic, message, timestamp)
    }

    override fun getAll(): Flow<List<DbFcmLog>> {
        return fcmDao.getAll()
    }
}