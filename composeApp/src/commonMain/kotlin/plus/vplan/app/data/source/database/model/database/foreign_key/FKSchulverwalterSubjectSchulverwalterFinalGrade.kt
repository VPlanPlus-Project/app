package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_schulverwalter_subject_schulverwalter_final_grade",
    primaryKeys = ["subject_id", "final_grade_id"],
    indices = [
        Index(value = ["subject_id"], unique = false),
        Index(value = ["final_grade_id"], unique = false)
    ]
)
data class FKSchulverwalterSubjectSchulverwalterFinalGrade(
    @ColumnInfo(name = "subject_id") val subjectId: Int,
    @ColumnInfo(name = "final_grade_id") val finalGradeId: Int
)
