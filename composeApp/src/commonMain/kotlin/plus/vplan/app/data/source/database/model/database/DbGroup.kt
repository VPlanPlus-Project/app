package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "school_groups",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["school_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbSchool::class,
            parentColumns = ["id"],
            childColumns = ["school_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbGroup(
    @ColumnInfo(name = "id") val id: Uuid,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "school_id") val schoolId: Uuid,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)