package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "substitution_plan_lesson",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["day_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbDay::class,
            parentColumns = ["id"],
            childColumns = ["day_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbLessonTime::class,
            parentColumns = ["id"],
            childColumns = ["lesson_time_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbDefaultLesson::class,
            parentColumns = ["id"],
            childColumns = ["default_lesson_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ]
)
data class DbSubstitutionPlanLesson(
    @ColumnInfo("id") val id: String,
    @ColumnInfo("day_id") val dayId: String,
    @ColumnInfo("lesson_time_id") val lessonTimeId: String,
    @ColumnInfo("subject") val subject: String?,
    @ColumnInfo("subject_changed") val subjectChanged: Boolean,
    @ColumnInfo("info") val info: String?,
    @ColumnInfo("default_lesson_id") val defaultLessonId: String?,
)
