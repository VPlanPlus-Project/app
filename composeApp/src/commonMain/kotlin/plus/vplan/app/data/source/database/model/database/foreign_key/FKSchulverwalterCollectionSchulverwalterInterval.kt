package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_schulverwalter_collection_schulverwalter_interval",
    primaryKeys = ["collection_id", "interval_id"],
    indices = [
        Index("collection_id", unique = false),
        Index("interval_id", unique = false)
    ]
)
data class FKSchulverwalterCollectionSchulverwalterInterval(
    @ColumnInfo("collection_id") val collectionId: Int,
    @ColumnInfo("interval_id") val intervalId: Int
)
