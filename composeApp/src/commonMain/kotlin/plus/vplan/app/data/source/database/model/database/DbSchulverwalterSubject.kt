package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.Instant

@Entity(
    tableName = "schulverwalter_subject",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class DbSchulverwalterSubject(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "local_id") val localId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "user_for_request") val userForRequest: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)
