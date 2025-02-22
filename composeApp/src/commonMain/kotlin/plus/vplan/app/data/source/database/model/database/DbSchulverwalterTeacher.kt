package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.Instant

@Entity(
    tableName = "schulverwalter_teacher",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class DbSchulverwalterTeacher(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "forename") val forename: String,
    @ColumnInfo(name = "surname") val surname: String,
    @ColumnInfo(name = "local_id") val localId: String,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)
