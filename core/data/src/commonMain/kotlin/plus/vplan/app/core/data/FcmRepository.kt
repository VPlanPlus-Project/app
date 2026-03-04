package plus.vplan.app.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.core.database.model.database.DbFcmLog
import plus.vplan.app.core.utils.date.now

interface FcmRepository {
    suspend fun log(
        topic: String,
        message: String,
        timestamp: LocalDateTime = LocalDateTime.now()
    )

    fun getAll(): Flow<List<DbFcmLog>>
}