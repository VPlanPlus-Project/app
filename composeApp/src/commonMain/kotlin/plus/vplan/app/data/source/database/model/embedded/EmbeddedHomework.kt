package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.domain.model.Homework

data class EmbeddedHomework(
    @Embedded val homework: DbHomework,
    @Relation(
        parentColumn = "id",
        entityColumn = "homework_id",
        entity = DbHomeworkTask::class
    ) val tasks: List<EmbeddedHomeworkTask>
) {
    fun toModel(): Homework {
        if (homework.id < 0) {
            return Homework.LocalHomework(
                id = homework.id,
                dueTo = homework.dueTo,
                createdAt = homework.createdAt,
                createdByProfile = homework.createdByProfileId!!,
                defaultLesson = homework.defaultLessonId,
                tasks = tasks.map { it.homeworkTask.id }
            )
        }
        return Homework.CloudHomework(
            id = homework.id,
            dueTo = homework.dueTo,
            createdAt = homework.createdAt,
            createdBy = homework.createdBy!!,
            defaultLesson = homework.defaultLessonId,
            group = homework.groupId,
            isPublic = homework.isPublic,
            tasks = tasks.map { it.homeworkTask.id }
        )
    }
}