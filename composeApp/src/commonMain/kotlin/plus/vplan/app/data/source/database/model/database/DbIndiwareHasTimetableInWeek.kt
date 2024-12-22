package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "indiware_has_timetable_in_week",
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
data class DbIndiwareHasTimetableInWeek(
    @ColumnInfo(name = "week_id") val weekId: String,
    @ColumnInfo(name = "has_data") val hasData: Boolean,
)
