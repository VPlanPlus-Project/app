package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import plus.vplan.app.core.model.File
import kotlin.time.Instant

@Entity(
    tableName = "file",
    indices = [Index("id")],
    primaryKeys = ["id"]
)
data class DbFile(
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("created_at") val createdAt: Instant,
    @ColumnInfo("created_by_vpp_id") val createdByVppId: Int?,
    @ColumnInfo("file_name") val fileName: String,
    @ColumnInfo("size") val size: Long,
    @ColumnInfo("is_offline_ready") val isOfflineReady: Boolean,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
) {
    fun toModel(): File {
        return File(
            id = id,
            name = fileName,
            size = size,
            isOfflineReady = isOfflineReady,
            cachedAt = cachedAt
        )
    }
}