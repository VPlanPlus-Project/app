package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.domain.model.Homework

data class EmbeddedHomeworkTask(
    @Embedded val homeworkTask: DbHomeworkTask
) {
    fun toModel(): Homework.HomeworkTask {
        return Homework.HomeworkTask(
            id = homeworkTask.id,
            content = homeworkTask.content,
            isDone = null
        )
    }
}
