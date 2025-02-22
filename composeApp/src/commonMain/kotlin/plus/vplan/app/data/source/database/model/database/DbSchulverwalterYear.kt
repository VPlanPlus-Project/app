package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.schulverwalter.Year

@Entity(
    tableName = "schulverwalter_year",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class DbSchulverwalterYear(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "from") val from: LocalDate,
    @ColumnInfo(name = "to") val to: LocalDate,
    @ColumnInfo(name = "user_for_request") val userForRequest: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
) {
    fun toModel(): Year {
        return Year(
            id = id,
            name = name,
            from = from,
            to = to,
            cachedAt = cachedAt
        )
    }
}
