package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "fcm_logs",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class DbFcmLog(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "timestamp") val timestamp: LocalDateTime,
    @ColumnInfo(name = "tag") val tag: String,
    @ColumnInfo(name = "message") val message: String,
)