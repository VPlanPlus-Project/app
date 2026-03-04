package plus.vplan.app.core.database.model.database.besteschule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import plus.vplan.app.core.model.besteschule.BesteSchuleSubject
import kotlin.time.Instant

@Entity(
    tableName = "besteschule_subjects",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class DbBesteschuleSubject(
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("short_name") val shortName: String,
    @ColumnInfo("long_name") val longName: String,
    @ColumnInfo("cached_at") val cachedAt: Instant,
) {
    fun toModel() = BesteSchuleSubject(
        id = this.id,
        shortName = this.shortName,
        fullName = this.longName,
        cachedAt = this.cachedAt
    )
}