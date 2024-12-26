package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "vpp_ids",
    primaryKeys = ["id"],
)
data class DbVppId(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
)