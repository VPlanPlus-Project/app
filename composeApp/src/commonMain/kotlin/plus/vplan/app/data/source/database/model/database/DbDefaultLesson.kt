package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.domain.model.EntityIdentifier
import kotlin.uuid.Uuid

@Entity(
    tableName = "default_lessons",
    primaryKeys = ["entity_id"],
    indices = [
        Index(value = ["entity_id"], unique = true),
        Index(value = ["teacher_id"], unique = false),
        Index(value = ["group_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbTeacher::class,
            parentColumns = ["entity_id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbDefaultLesson(
    @ColumnInfo(name = "entity_id") val id: Uuid = Uuid.random(),
    @ColumnInfo(name = "subject") val subject: String,
    @ColumnInfo(name = "teacher_id") val teacherId: Uuid?,
    @ColumnInfo(name = "group_id") val groupId: Int,
    @ColumnInfo(name = "course_id") val courseId: Uuid?
)

@Entity(
    tableName = "default_lesson_identifiers",
    primaryKeys = ["default_lesson_id", "origin", "value"],
    indices = [Index(value = ["default_lesson_id", "origin", "value"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = DbDefaultLesson::class,
            parentColumns = ["entity_id"],
            childColumns = ["default_lesson_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbDefaultLessonIdentifier(
    @ColumnInfo(name = "default_lesson_id") val defaultLessonId: Uuid,
    @ColumnInfo(name = "origin") val origin: EntityIdentifier.Origin,
    @ColumnInfo(name = "value") val value: String
) {
    fun toModel(): EntityIdentifier {
        return EntityIdentifier(
            entityId = defaultLessonId,
            origin = origin,
            value = value
        )
    }
}