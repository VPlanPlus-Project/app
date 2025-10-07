package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "substitution_plan_lesson",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["day_id"], unique = false),
        Index(value = ["subject_instance_id"], unique = false),
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
            entity = DbSubjectInstance::class,
            parentColumns = ["id"],
            childColumns = ["subject_instance_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ]
)
data class DbSubstitutionPlanLesson(
    @ColumnInfo("id") val id: Uuid,
    @ColumnInfo("day_id") val dayId: String,
    @ColumnInfo("lesson_number") val lessonNumber: Int,
    @ColumnInfo("subject") val subject: String?,
    @ColumnInfo("is_subject_changed") val isSubjectChanged: Boolean,
    @ColumnInfo("info") val info: String?,
    @ColumnInfo("subject_instance_id") val subjectInstanceId: Uuid?,
    @ColumnInfo("is_room_changed") val isRoomChanged: Boolean,
    @ColumnInfo("is_teacher_changed") val isTeacherChanged: Boolean
)
