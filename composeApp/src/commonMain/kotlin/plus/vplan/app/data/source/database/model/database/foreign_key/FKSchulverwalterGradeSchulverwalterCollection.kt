package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_schulverwalter_grade_schulverwalter_collection",
    primaryKeys = ["grade_id", "collection_id"],
    indices = [
        Index("grade_id", unique = false),
        Index("collection_id", unique = false)
    ]
)
data class FKSchulverwalterGradeSchulverwalterCollection(
    @ColumnInfo("grade_id") val gradeId: Int,
    @ColumnInfo("collection_id") val collectionId: Int
)