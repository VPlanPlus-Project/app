package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "schulverwalter_collection",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
    ]
)
data class DbSchulverwalterCollection(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "given_at") val givenAt: LocalDate,
    @ColumnInfo(name = "user_for_request") val userForRequest: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant,
)
