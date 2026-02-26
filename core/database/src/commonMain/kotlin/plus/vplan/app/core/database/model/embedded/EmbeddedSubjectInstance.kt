package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbGroup
import plus.vplan.app.core.database.model.database.DbSubjectInstance
import plus.vplan.app.core.database.model.database.DbSubjectInstanceAlias
import plus.vplan.app.core.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.core.model.SubjectInstance

data class EmbeddedSubjectInstance(
    @Embedded val subjectInstance: DbSubjectInstance,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = DbGroup::class,
        associateBy = Junction(
            value = FKSubjectInstanceGroup::class,
            parentColumn = "subject_instance_id",
            entityColumn = "group_id"
        )
    ) val groups: List<EmbeddedGroup>,
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
            teacherId = subjectInstance.teacherId,
            groups = groups.flatMap{ it.aliases.map { alias -> alias.toModel() } },
            courseId = subjectInstance.courseId,
            cachedAt = subjectInstance.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}