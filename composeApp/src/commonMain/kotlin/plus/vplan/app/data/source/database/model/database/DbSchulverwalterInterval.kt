package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "schulverwalter_interval",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = DbSchulverwalterYear::class,
            parentColumns = ["id"],
            childColumns = ["year_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbSchulverwalterInterval::class,
            parentColumns = ["id"],
            childColumns = ["included_interval_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["year_id"], unique = false),
        Index(value = ["included_interval_id"], unique = false)
    ]
)
data class DbSchulverwalterInterval(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "from") val from: LocalDate,
    @ColumnInfo(name = "to") val to: LocalDate,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "included_interval_id") val includedIntervalId: Int?,
    @ColumnInfo(name = "year_id") val yearId: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)
