package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_schulverwalter_grade_schulverwalter_teacher",
    primaryKeys = ["grade_id", "teacher_id"],
    indices = [
        Index("grade_id", unique = false),
        Index("teacher_id", unique = false)
    ]
)
data class FKSchulverwalterGradeSchulverwalterTeacher(
    @ColumnInfo("grade_id") val gradeId: Int,
    @ColumnInfo("teacher_id") val teacherId: Int
)