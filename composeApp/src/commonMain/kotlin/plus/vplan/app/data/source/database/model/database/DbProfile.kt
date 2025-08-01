package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "profiles",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["school_id"], unique = false)
    ],
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = DbSchool::class,
            parentColumns = ["id"],
            childColumns = ["school_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbProfile(
    @ColumnInfo(name = "id") val id: Uuid,
    @ColumnInfo(name = "school_id") val schoolId: Uuid,
    @ColumnInfo(name = "display_name") val displayName: String?
)
