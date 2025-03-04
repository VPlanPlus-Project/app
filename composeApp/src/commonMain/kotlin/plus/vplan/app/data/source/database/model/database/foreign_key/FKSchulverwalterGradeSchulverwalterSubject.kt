package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_schulverwalter_grade_schulverwalter_subject",
    primaryKeys = ["grade_id", "subject_id"],
    indices = [
        Index("grade_id", unique = false),
        Index("subject_id", unique = false)
    ]
)
data class FKSchulverwalterGradeSchulverwalterSubject(
    @ColumnInfo("grade_id") val gradeId: Int,
    @ColumnInfo("subject_id") val subjectId: Int
)