package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_schulverwalter_year_schulverwalter_interval",
    primaryKeys = ["year_id", "interval_id"],
    indices = [
        Index("year_id", unique = false),
        Index("interval_id", unique = false)
    ]
)
data class FKSchulverwalterYearSchulverwalterInterval(
    @ColumnInfo("year_id") val yearId: Int,
    @ColumnInfo("interval_id") val intervalId: Int
)
