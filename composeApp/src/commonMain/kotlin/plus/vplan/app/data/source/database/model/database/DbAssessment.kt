package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Entity(
    tableName = "assessments",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = DbDefaultLesson::class,
            parentColumns = ["id"],
            childColumns = ["default_lesson_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbProfile::class,
            parentColumns = ["id"],
            childColumns = ["created_by_profile"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbAssessment(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "created_by") val createdBy: Int?,
    @ColumnInfo(name = "created_by_profile") val createdByProfile: Uuid?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "is_public") val isPublic: Boolean,
    @ColumnInfo(name = "default_lesson_id") val defaultLessonId: Int,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)