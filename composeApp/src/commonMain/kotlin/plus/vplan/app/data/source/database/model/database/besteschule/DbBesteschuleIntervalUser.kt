package plus.vplan.app.data.source.database.model.database.besteschule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "besteschule_interval_user",
    primaryKeys = ["interval_id", "schulverwalter_user_id"],
    indices = [
        Index(value = ["interval_id"]),
        Index(value = ["schulverwalter_user_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbBesteSchuleInterval::class,
            parentColumns = ["id"],
            childColumns = ["interval_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class DbBesteschuleIntervalUser(
    @ColumnInfo("interval_id") val intervalId: Int,
    @ColumnInfo("schulverwalter_user_id") val schulverwalterUserId: Int
)
