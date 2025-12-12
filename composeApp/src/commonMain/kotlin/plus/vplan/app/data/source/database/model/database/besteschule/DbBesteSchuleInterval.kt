package plus.vplan.app.data.source.database.model.database.besteschule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Entity(
    tableName = "besteschule_intervals",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["year_id"], unique = false),
        Index(value = ["included_interval_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbBesteschuleYear::class,
            parentColumns = ["id"],
            childColumns = ["year_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbBesteSchuleInterval::class,
            parentColumns = ["id"],
            childColumns = ["included_interval_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbBesteSchuleInterval(
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("type") val type: String,
    @ColumnInfo("from") val from: LocalDate,
    @ColumnInfo("to") val to: LocalDate,
    @ColumnInfo("included_interval_id") val includedIntervalId: Int?,
    @ColumnInfo("year_id") val yearId: Int,
    @ColumnInfo("cached_at") val cachedAt: Instant
)