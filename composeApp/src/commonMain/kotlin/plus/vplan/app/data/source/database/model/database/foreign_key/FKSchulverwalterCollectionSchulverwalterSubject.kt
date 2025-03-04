package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_schulverwalter_collection_schulverwalter_subject",
    primaryKeys = ["collection_id", "subject_id"],
    indices = [
        Index("collection_id", unique = false),
        Index("subject_id", unique = false)
    ]
)
data class FKSchulverwalterCollectionSchulverwalterSubject(
    @ColumnInfo("collection_id") val collectionId: Int,
    @ColumnInfo("subject_id") val subjectId: Int
)
