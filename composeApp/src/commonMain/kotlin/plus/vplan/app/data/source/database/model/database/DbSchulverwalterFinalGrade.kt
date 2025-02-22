package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.Instant

@Entity(
    tableName = "schulverwalter_final_grade",
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
data class DbSchulverwalterFinalGrade(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "calculation_rule") val calculationRule: String?,
    @ColumnInfo(name = "subject_id") val subjectId: Int,
    @ColumnInfo(name = "interval_id") val intervalId: Int,
    @ColumnInfo(name = "user_for_request") val userForRequest: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)
