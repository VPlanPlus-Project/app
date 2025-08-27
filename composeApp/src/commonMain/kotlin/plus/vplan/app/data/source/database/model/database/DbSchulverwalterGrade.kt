@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Entity(
    tableName = "schulverwalter_grade",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class DbSchulverwalterGrade(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "value") val value: String?,
    @ColumnInfo(name = "is_optional") val isOptional: Boolean,
    @ColumnInfo(name = "is_selected_for_final_grade") val isSelectedForFinalGrade: Boolean,
    @ColumnInfo(name = "user_for_request") val userForRequest: Int,
    @ColumnInfo(name = "vpp_id") val vppId: Int,
    @ColumnInfo(name = "given_at") val givenAt: LocalDate,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)
