package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbHomework
import plus.vplan.app.core.database.model.database.DbHomeworkTask
import plus.vplan.app.core.database.model.database.DbProfile
import plus.vplan.app.core.database.model.database.DbVppId
import plus.vplan.app.core.database.model.database.foreign_key.FKHomeworkFile
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile

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
    ) val files: List<FKHomeworkFile>,
    @Relation(
        parentColumn = "created_by_profile_id",
        entityColumn = "id",
        entity = DbProfile::class
    ) val createdByProfileId: EmbeddedProfile?,
    @Relation(
        parentColumn = "created_by_vpp_id",
        entityColumn = "id",
        entity = DbVppId::class
    ) val createdBy: EmbeddedVppId?,
) {
    fun toModel(): Homework {
        if (homework.id < 0) {
            return Homework.LocalHomework(
                id = homework.id,
                dueTo = homework.dueTo,
                createdAt = homework.createdAt,
                createdByProfile = createdByProfileId?.toModel() as Profile.StudentProfile,
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
            createdBy = createdBy!!.toModel(),
            subjectInstanceId = homework.subjectInstanceId,
            groupId = homework.groupId,
            isPublic = homework.isPublic,
            fileIds = files.map { it.fileId },
            taskIds = tasks.map { it.id },
            cachedAt = homework.cachedAt
        )
    }
}