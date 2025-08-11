package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.CreationReason

@Entity(
    tableName = "vpp_id",
    primaryKeys = ["id"],
)
data class DbVppId(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant,
    @ColumnInfo(name = "creation_reason") val creationReason: CreationReason
)