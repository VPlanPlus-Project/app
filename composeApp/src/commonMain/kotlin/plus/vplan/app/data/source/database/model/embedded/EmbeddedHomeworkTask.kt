package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneAccount
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneProfile
import plus.vplan.app.domain.model.Homework.HomeworkTask

data class EmbeddedHomeworkTask(
    @Embedded val homeworkTask: DbHomeworkTask,
    @Relation(
        parentColumn = "id",
        entityColumn = "task_id",
        entity = DbHomeworkTaskDoneAccount::class
    ) val doneAccounts: List<DbHomeworkTaskDoneAccount>,
    @Relation(
        parentColumn = "id",
        entityColumn = "task_id",
        entity = DbHomeworkTaskDoneProfile::class
    ) val doneProfiles: List<DbHomeworkTaskDoneProfile>,
) {
    fun toModel(): HomeworkTask {
        return HomeworkTask(
            id = homeworkTask.id,
            content = homeworkTask.content,
            doneByProfiles = doneProfiles.filter { it.isDone }.map { it.profileId },
            doneByVppIds = doneAccounts.filter { it.isDone }.map { it.vppId },
            homework = homeworkTask.homeworkId,
            cachedAt = homeworkTask.cachedAt
        )
    }
}