package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.domain.model.Homework

@Entity(
    tableName = "homework_task",
    primaryKeys = ["id"],
    indices = [
        Index("id", unique = true),
        Index("homework_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbHomework::class,
            parentColumns = ["id"],
            childColumns = ["homework_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbHomeworkTask(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "homework_id") val homeworkId: Int,
    @ColumnInfo(name = "content") val content: String,
) {
    fun toModel(): Homework.HomeworkTask {
        return Homework.HomeworkTask(
            id = id,
            content = content,
            homework = homeworkId,
            isDone = null
        )
    }
}