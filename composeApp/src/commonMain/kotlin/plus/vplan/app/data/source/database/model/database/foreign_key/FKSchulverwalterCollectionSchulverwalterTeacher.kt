package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_schulverwalter_collection_schulverwalter_teacher",
    primaryKeys = ["collection_id", "teacher_id"],
    indices = [
        Index("collection_id", unique = false),
        Index("teacher_id", unique = false)
    ]
)
data class FKSchulverwalterCollectionSchulverwalterTeacher(
    @ColumnInfo("collection_id") val collectionId: Int,
    @ColumnInfo("teacher_id") val teacherId: Int
)
