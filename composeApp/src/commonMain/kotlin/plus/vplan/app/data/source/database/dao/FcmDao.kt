package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.data.source.database.model.database.DbFcmLog

@Dao
interface FcmDao {

    @Query("INSERT INTO fcm_logs (id, tag, message, timestamp) VALUES (:id, :tag, :message, :timestamp)")
    suspend fun log(
        id: Int,
        tag: String,
        message: String,
        timestamp: LocalDateTime
    )

    @Query("SELECT * FROM fcm_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<DbFcmLog>>

    @Query("SELECT MAX(CAST(id AS INTEGER)) FROM fcm_logs")
    fun getMaxId(): Flow<Int?>
}