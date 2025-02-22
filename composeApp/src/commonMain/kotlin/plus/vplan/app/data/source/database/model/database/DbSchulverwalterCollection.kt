package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.Instant

@Entity(
    tableName = "schulverwalter_collection",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = DbSchulverwalterSubject::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbSchulverwalterInterval::class,
            parentColumns = ["id"],
            childColumns = ["interval_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["subject_id"], unique = false),
        Index(value = ["interval_id"], unique = false)
    ]
)
data class DbSchulverwalterCollection(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "subject_id") val subjectId: Int,
    @ColumnInfo(name = "interval_id") val intervalId: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)
