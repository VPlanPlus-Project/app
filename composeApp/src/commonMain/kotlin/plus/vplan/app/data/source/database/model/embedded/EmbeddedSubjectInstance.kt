package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.domain.model.SubjectInstance

data class EmbeddedSubjectInstance(
    @Embedded val subjectInstance: DbSubjectInstance,
    @Relation(
        parentColumn = "id",
        entityColumn = "subject_instance_id",
        entity = FKSubjectInstanceGroup::class
    ) val groups: List<FKSubjectInstanceGroup>
) {
    fun toModel(): SubjectInstance {
        return SubjectInstance(
            id = subjectInstance.id,
            indiwareId = subjectInstance.indiwareId,
            subject = subjectInstance.subject,
            teacher = subjectInstance.teacherId,
            groups = groups.map { it.groupId },
            course = subjectInstance.courseId,
            cachedAt = subjectInstance.cachedAt
        )
    }
}