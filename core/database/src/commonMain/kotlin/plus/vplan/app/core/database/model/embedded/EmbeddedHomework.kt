package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbFile
import plus.vplan.app.core.database.model.database.DbGroup
import plus.vplan.app.core.database.model.database.DbHomework
import plus.vplan.app.core.database.model.database.DbHomeworkTask
import plus.vplan.app.core.database.model.database.DbProfile
import plus.vplan.app.core.database.model.database.DbSubjectInstance
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
    ) val tasks: List<EmbeddedHomeworkTask>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = DbFile::class,
        associateBy = Junction(
            parentColumn = "file_id",
            entityColumn = "homework_id",
            value = FKHomeworkFile::class
        )
    ) val files: List<DbFile>,
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
    @Relation(
        parentColumn = "subject_instance_id",
        entityColumn = "id",
        entity = DbSubjectInstance::class
    ) val subjectInstance: EmbeddedSubjectInstance?,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "id",
        entity = DbGroup::class
    ) val group: EmbeddedGroup?
) {
    fun toModel(): Homework {
        if (homework.id < 0) {
            return Homework.LocalHomework(
                id = homework.id,
                dueTo = homework.dueTo,
                createdAt = homework.createdAt,
                createdByProfile = createdByProfileId?.toModel() as Profile.StudentProfile,
                subjectInstance = subjectInstance?.toModel(),
                files = files.map { it.toModel() },
                tasks = tasks.map { it.toModel() },
                cachedAt = homework.cachedAt,
                group = group?.toModel(),
            )
        }
        return Homework.CloudHomework(
            id = homework.id,
            dueTo = homework.dueTo,
            createdAt = homework.createdAt,
            createdBy = createdBy!!.toModel(),
            subjectInstance = subjectInstance?.toModel(),
            group = group?.toModel(),
            isPublic = homework.isPublic,
            files = files.map { it.toModel() },
            tasks = tasks.map { it.toModel() },
            cachedAt = homework.cachedAt
        )
    }
}