package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "default_lessons",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["teacher_id"], unique = false),
        Index(value = ["group_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbTeacher::class,
            parentColumns = ["id"],
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
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "subject") val subject: String,
    @ColumnInfo(name = "teacher_id") val teacherId: Int?,
    @ColumnInfo(name = "group_id") val groupId: Int,
    @ColumnInfo(name = "course_id") val courseId: String?
)