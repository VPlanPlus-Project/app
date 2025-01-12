package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Holiday

@Entity(
    tableName = "holidays",
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
data class DbHoliday(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "school_id") val schoolId: Int,
) {
    fun toModel(): Holiday {
        return Holiday(
            id = id,
            date = date,
            school = schoolId
        )
    }
}