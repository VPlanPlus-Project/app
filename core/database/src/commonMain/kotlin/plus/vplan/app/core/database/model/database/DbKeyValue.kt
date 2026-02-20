package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "key_value",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class DbKeyValue(
    @ColumnInfo(name = "id") val key: String,
    @ColumnInfo(name = "value") val value: String
)
