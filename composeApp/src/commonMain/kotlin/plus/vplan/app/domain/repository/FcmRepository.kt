package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.data.source.database.model.database.DbFcmLog
import plus.vplan.app.utils.now

interface FcmRepository {
    suspend fun log(
        topic: String,
        message: String,
        timestamp: LocalDateTime = LocalDateTime.now()
    )

    fun getAll(): Flow<List<DbFcmLog>>
}