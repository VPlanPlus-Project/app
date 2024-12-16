package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "schools",
    primaryKeys = ["id"],
    indices = [Index(value = ["id"], unique = true)]
)
data class DbSchool(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String
)