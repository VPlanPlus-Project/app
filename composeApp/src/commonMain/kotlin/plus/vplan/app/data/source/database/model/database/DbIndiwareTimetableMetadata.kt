package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "indiware_timetable_metadata",
    primaryKeys = ["week_id"],
    indices = [
        Index(value = ["week_id"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbWeek::class,
            parentColumns = ["id"],
            childColumns = ["week_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbIndiwareTimetableMetadata(
    @ColumnInfo(name = "indiware_school_id") val indiwareSchoolId: String,
    @ColumnInfo(name = "week_id") val weekId: String,
    @ColumnInfo(name = "has_data") val hasData: Boolean
)
