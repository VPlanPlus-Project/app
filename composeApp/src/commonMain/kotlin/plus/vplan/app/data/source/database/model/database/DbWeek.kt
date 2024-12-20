package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Entity(
    tableName = "weeks",
    primaryKeys = ["school_id", "calendar_week"],
    foreignKeys = [
        ForeignKey(
            entity = DbSchool::class,
            parentColumns = ["id"],
            childColumns = ["school_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbWeek(
    @ColumnInfo(name = "id") val id: Uuid,
    @ColumnInfo(name = "school_id") val schoolId: Int,
    @ColumnInfo(name = "calendar_week") val calendarWeek: Int,
    @ColumnInfo(name = "start") val start: LocalDate,
    @ColumnInfo(name = "end") val end: LocalDate, // TODO add adapter
    @ColumnInfo(name = "week_type") val weekType: String,
    @ColumnInfo(name = "week_index") val weekIndex: Int
)
