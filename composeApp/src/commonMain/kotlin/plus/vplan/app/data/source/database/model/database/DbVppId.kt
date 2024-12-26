package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "vpp_id",
    primaryKeys = ["id"],
)
data class DbVppId(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "cached_at") val cachedAt: LocalDateTime
)