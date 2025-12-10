package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Stores, which intervals are relevant for a Schulverwalter User
 */
@Entity(
    tableName = "schulverwalter_interval_user",
    primaryKeys = ["interval_id", "schulverwalter_user_id"],
    foreignKeys = [
        ForeignKey(
            entity = DbSchulverwalterInterval::class,
            parentColumns = ["id"],
            childColumns = ["interval_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["interval_id"], unique = false),
        Index(value = ["schulverwalter_user_id"], unique = false)
    ]
)
data class DbSchulverwalterIntervalUser(
    @ColumnInfo("interval_id") val intervalId: Int,
    @ColumnInfo("schulverwalter_user_id") val schulverwalterUserId: Int
)