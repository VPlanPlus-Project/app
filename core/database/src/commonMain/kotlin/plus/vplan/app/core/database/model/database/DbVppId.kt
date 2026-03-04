@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import plus.vplan.app.core.model.CreationReason
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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