package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.foreign_key.FKHomeworkFile
import plus.vplan.app.core.model.Homework
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
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
                createdByProfileId = homework.createdByProfileId!!,
                subjectInstanceId = homework.subjectInstanceId,
                fileIds = files.map { it.fileId },
                taskIds = tasks.map { it.id },
                cachedAt = homework.cachedAt,
                groupId = homework.groupId
            )
        }
        return Homework.CloudHomework(
            id = homework.id,
            dueTo = homework.dueTo,
            createdAt = homework.createdAt,
            createdById = homework.createdBy!!,
            subjectInstanceId = homework.subjectInstanceId,
            groupId = homework.groupId,
            isPublic = homework.isPublic,
            fileIds = files.map { it.fileId },
            taskIds = tasks.map { it.id },
            cachedAt = homework.cachedAt
        )
    }
}