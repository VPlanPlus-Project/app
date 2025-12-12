package plus.vplan.app.data.source.database.model.database.besteschule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import plus.vplan.app.domain.model.besteschule.BesteSchuleTeacher
import kotlin.time.Instant

@Entity(
    tableName = "besteschule_teacher",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class DbBesteschuleTeacher(
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("forename") val forename: String,
    @ColumnInfo("surname") val surname: String,
    @ColumnInfo("local_id") val localId: String,
    @ColumnInfo("cached_at") val cachedAt: Instant
) {
    fun toModel() = BesteSchuleTeacher(
        id = this.id,
        forename = this.forename,
        surname = this.surname,
        localId = this.localId,
        cachedAt = this.cachedAt
    )
}