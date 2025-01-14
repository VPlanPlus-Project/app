package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkFile
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.domain.model.Homework

data class EmbeddedHomework(
    @Embedded val homework: DbHomework,
    @Relation(
        parentColumn = "id",
        entityColumn = "homework_id",
        entity = DbHomeworkTask::class
    ) val tasks: List<EmbeddedHomeworkTask>,
    @Relation(
        parentColumn = "id",
        entityColumn = "homework_id",
        entity = DbHomeworkFile::class
    ) val files: List<DbHomeworkFile>
) {
    fun toModel(): Homework {
        if (homework.id < 0) {
            return Homework.LocalHomework(
                id = homework.id,
                dueTo = homework.dueTo,
                createdAt = homework.createdAt,
                createdByProfile = homework.createdByProfileId!!,
                defaultLesson = homework.defaultLessonId,
                files = files.map { it.id },
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
            files = files.map { it.id },
            tasks = tasks.map { it.homeworkTask.id }
        )
    }
}