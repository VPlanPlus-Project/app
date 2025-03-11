package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.data.source.database.model.database.DbAssessment
import plus.vplan.app.data.source.database.model.database.foreign_key.FKAssessmentFile
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment

data class EmbeddedAssessment(
    @Embedded val assessment: DbAssessment,
    @Relation(
        parentColumn = "id",
        entityColumn = "assessment_id",
        entity = FKAssessmentFile::class
    ) val files: List<FKAssessmentFile>
) {
    fun toModel() = Assessment(
        id = assessment.id,
        creator = if (assessment.createdBy != null) AppEntity.VppId(assessment.createdBy) else AppEntity.Profile(assessment.createdByProfile!!),
        createdAt = assessment.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()),
        date = assessment.date,
        isPublic = assessment.isPublic,
        subjectInstanceId = assessment.subjectInstanceId,
        description = assessment.description,
        type = Assessment.Type.entries[assessment.type],
        files = files.map { it.fileId },
        cachedAt = assessment.cachedAt
    )
}