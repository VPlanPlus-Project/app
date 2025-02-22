package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.Instant

@Entity(
    tableName = "schulverwalter_grade",
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
        ),
        ForeignKey(
            entity = DbSchulverwalterTeacher::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbSchulverwalterCollection::class,
            parentColumns = ["id"],
            childColumns = ["collection_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["subject_id"], unique = false),
        Index(value = ["interval_id"], unique = false),
        Index(value = ["teacher_id"], unique = false),
        Index(value = ["collection_id"], unique = false)
    ]
)
data class DbSchulverwalterGrade(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "value") val value: String?,
    @ColumnInfo(name = "is_optional") val isOptional: Boolean,
    @ColumnInfo(name = "is_selected_for_final_grade") val isSelectedForFinalGrade: Boolean,
    @ColumnInfo(name = "subject_id") val subjectId: Int,
    @ColumnInfo(name = "interval_id") val intervalId: Int,
    @ColumnInfo(name = "teacher_id") val teacherId: Int,
    @ColumnInfo(name = "collection_id") val collectionId: Int,
    @ColumnInfo(name = "user_for_request") val userForRequest: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)
