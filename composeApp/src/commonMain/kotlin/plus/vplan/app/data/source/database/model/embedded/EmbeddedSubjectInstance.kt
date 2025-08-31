package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.DbSubjectInstanceAlias
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.domain.model.SubjectInstance

data class EmbeddedSubjectInstance(
    @Embedded val subjectInstance: DbSubjectInstance,
    @Relation(
        parentColumn = "id",
        entityColumn = "subject_instance_id",
        entity = FKSubjectInstanceGroup::class
    ) val groups: List<FKSubjectInstanceGroup>,
    @Relation(
        parentColumn = "id",
        entityColumn = "subject_instance_id",
        entity = DbSubjectInstanceAlias::class
    ) val aliases: List<DbSubjectInstanceAlias>
) {
    fun toModel(): SubjectInstance {
        return SubjectInstance(
            id = subjectInstance.id,
            subject = subjectInstance.subject,
            teacher = subjectInstance.teacherId,
            groupIds = groups.map { it.groupId },
            courseId = subjectInstance.courseId,
            cachedAt = subjectInstance.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}