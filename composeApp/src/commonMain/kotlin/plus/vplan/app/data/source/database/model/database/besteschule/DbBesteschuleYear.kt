package plus.vplan.app.data.source.database.model.database.besteschule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.besteschule.BesteSchuleYear
import kotlin.time.Instant

@Entity(
    tableName = "besteschule_year",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class DbBesteschuleYear(
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("from") val from: LocalDate,
    @ColumnInfo("to") val to: LocalDate,
    @ColumnInfo("cached_at") val cachedAt: Instant
) {
    fun toModel() = BesteSchuleYear(
        id = this.id,
        name = this.name,
        from = this.from,
        to = this.to,
        cachedAt = this.cachedAt
    )
}