package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.foreign_key.FKHomeworkFile
import plus.vplan.app.domain.model.Homework

data class EmbeddedHomework(
    @Embedded val homework: DbHomework,
    @Relation(
        parentColumn = "id",
        entityColumn = "homework_id",
        entity = DbHomeworkTask::class
    ) val tasks: List<DbHomeworkTask>,
    @Relation(
        parentColumn = "id",
        entityColumn = "homework_id",
        entity = FKHomeworkFile::class
    ) val files: List<FKHomeworkFile>
) {
    fun toModel(): Homework {
        if (homework.id < 0) {
            return Homework.LocalHomework(
                id = homework.id,
                dueTo = homework.dueTo,
                createdAt = homework.createdAt,
                createdByProfile = homework.createdByProfileId!!,
                subjectInstanceId = homework.subjectInstanceId,
                files = files.map { it.fileId },
                taskIds = tasks.map { it.id },
                cachedAt = homework.cachedAt,
                groupId = homework.groupId
            )
        }
        return Homework.CloudHomework(
            id = homework.id,
            dueTo = homework.dueTo,
            createdAt = homework.createdAt,
            createdBy = homework.createdBy!!,
            subjectInstanceId = homework.subjectInstanceId,
            groupId = homework.groupId,
            isPublic = homework.isPublic,
            files = files.map { it.fileId },
            taskIds = tasks.map { it.id },
            cachedAt = homework.cachedAt
        )
    }
}