package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "vpp_id_access",
    primaryKeys = ["vpp_id"],
    indices = [
        Index(value = ["vpp_id"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbVppId::class,
            parentColumns = ["id"],
            childColumns = ["vpp_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbVppIdAccess(
    @ColumnInfo(name = "vpp_id") val vppId: Int,
    @ColumnInfo(name = "access_token") val accessToken: String
)
