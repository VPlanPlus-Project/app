package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.Instant
import plus.vplan.app.domain.model.File

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
) {
    fun toModel(): File {
        return File(
            id = id,
            name = fileName,
            size = size,
            isOfflineReady = isOfflineReady,
            getBitmap = { null }
        )
    }
}