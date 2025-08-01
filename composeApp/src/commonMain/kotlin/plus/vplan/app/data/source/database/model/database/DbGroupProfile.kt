package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "profiles_group",
    primaryKeys = ["profile_id"],
    indices = [
        Index(value = ["profile_id"], unique = true),
        Index(value = ["group_id"], unique = false),
        Index(value = ["vpp_id"], unique = true)
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
            entity = DbGroup::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbVppId::class,
            parentColumns = ["id"],
            childColumns = ["vpp_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbGroupProfile(
    @ColumnInfo(name = "profile_id") val profileId: Uuid,
    @ColumnInfo(name = "group_id") val groupId: Uuid,
    @ColumnInfo(name = "vpp_id") val vppId: Int?
)