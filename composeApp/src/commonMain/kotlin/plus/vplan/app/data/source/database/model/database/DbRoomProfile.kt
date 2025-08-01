package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "profiles_room",
    primaryKeys = ["profile_id"],
    indices = [
        Index(value = ["profile_id"], unique = true),
        Index(value = ["room_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbProfile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbRoom::class,
            parentColumns = ["id"],
            childColumns = ["room_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbRoomProfile(
    @ColumnInfo(name = "profile_id") val profileId: Uuid,
    @ColumnInfo(name = "room_id") val roomId: Uuid,
)