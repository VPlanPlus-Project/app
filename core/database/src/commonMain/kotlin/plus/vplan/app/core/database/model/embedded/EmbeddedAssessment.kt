package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.core.database.model.database.DbAssessment
import plus.vplan.app.core.database.model.database.DbProfile
import plus.vplan.app.core.database.model.database.DbSubjectInstance
import plus.vplan.app.core.database.model.database.DbVppId
import plus.vplan.app.core.database.model.database.foreign_key.FKAssessmentFile
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.Assessment

data class EmbeddedAssessment(
    @Embedded val assessment: DbAssessment,
    @Relation(
        parentColumn = "id",
        entityColumn = "assessment_id",
        entity = FKAssessmentFile::class
    ) val files: List<FKAssessmentFile>,
    @Relation(
        parentColumn = "subject_instance_id",
        entityColumn = "id",
        entity = DbSubjectInstance::class
    ) val subjectInstance: EmbeddedSubjectInstance,
    @Relation(
        parentColumn = "created_by_profile",
        entityColumn = "id",
        entity = DbProfile::class
    ) val createdByProfile: EmbeddedProfile?,
    @Relation(
        parentColumn = "created_by",
        entityColumn = "id",
        entity = DbVppId::class
    ) val createdBy: EmbeddedVppId?
) {
    fun toModel() = Assessment(
        id = assessment.id,
        creator = if (assessment.createdBy != null) AppEntity.VppId(createdBy!!.toModel()) else AppEntity.Profile(createdByProfile!!.toModel()!!),
        createdAt = assessment.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()),
        date = assessment.date,
        isPublic = assessment.isPublic,
        subjectInstance = subjectInstance.toModel(),
        description = assessment.description,
        type = Assessment.Type.entries[assessment.type],
        fileIds = files.map { it.fileId },
        cachedAt = assessment.cachedAt
    )
}