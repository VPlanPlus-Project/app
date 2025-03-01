package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "default_lessons",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["teacher_id"], unique = false)
    ]
)
data class DbDefaultLesson(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo("indiware_id") val indiwareId: String?,
    @ColumnInfo(name = "subject") val subject: String,
    @ColumnInfo(name = "teacher_id") val teacherId: Int?,
    @ColumnInfo(name = "course_id") val courseId: Int?
)