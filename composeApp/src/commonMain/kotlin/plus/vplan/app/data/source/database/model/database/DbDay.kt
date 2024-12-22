package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "day",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["school_id"], unique = false)
    ],
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
data class DbDay(
    @ColumnInfo("id") val id: String,
    @ColumnInfo("date") val date: LocalDate,
    @ColumnInfo("school_id") val schoolId: Int,
    @ColumnInfo("info") val info: String?,
)