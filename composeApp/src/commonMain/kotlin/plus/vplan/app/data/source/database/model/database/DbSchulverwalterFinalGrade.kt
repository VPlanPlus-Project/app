@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Entity(
    tableName = "schulverwalter_final_grade",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
    ]
)
data class DbSchulverwalterFinalGrade(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "calculation_rule") val calculationRule: String,
    @ColumnInfo(name = "user_for_request") val userForRequest: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)
