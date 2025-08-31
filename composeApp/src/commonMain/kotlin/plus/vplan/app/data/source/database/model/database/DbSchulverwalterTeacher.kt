@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import plus.vplan.app.domain.model.schulverwalter.Teacher
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Entity(
    tableName = "schulverwalter_teacher",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class DbSchulverwalterTeacher(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "forename") val forename: String,
    @ColumnInfo(name = "surname") val surname: String,
    @ColumnInfo(name = "local_id") val localId: String,
    @ColumnInfo(name = "user_for_request") val userForRequest: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
) {
    fun toModel() = Teacher(
        id = id,
        forename = forename,
        name = surname,
        localId = localId,
        cachedAt = cachedAt
    )
}