package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterFinalGrade
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterSubjectSchulverwalterFinalGrade
import plus.vplan.app.domain.model.schulverwalter.FinalGrade

data class EmbeddedSchulverwalterFinalGrade(
    @Embedded val finalGrade: DbSchulverwalterFinalGrade,
    @Relation(
        parentColumn = "id",
        entityColumn = "final_grade_id",
        entity = FKSchulverwalterSubjectSchulverwalterFinalGrade::class
    ) val subject: FKSchulverwalterSubjectSchulverwalterFinalGrade
) {
    fun toModel(): FinalGrade {
        return FinalGrade(
            id = finalGrade.id,
            calculationRule = finalGrade.calculationRule,
            subjectId = subject.finalGradeId,
            cachedAt = finalGrade.cachedAt
        )
    }
}
