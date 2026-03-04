package plus.vplan.app.core.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_assessment_file",
    primaryKeys = ["assessment_id", "file_id"],
    indices = [
        Index("assessment_id", unique = false),
        Index("file_id", unique = false)
    ]
)
data class FKAssessmentFile(
    @ColumnInfo("assessment_id") val assessmentId: Int,
    @ColumnInfo("file_id") val fileId: Int
)