package plus.vplan.app.core.database.model.database.crossovers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.core.database.model.database.DbVppId

@Entity(
    tableName = "vpp_id_group_crossover",
    primaryKeys = ["vpp_id", "group_id"],
    indices = [
        Index(value = ["vpp_id"], unique = false),
        Index(value = ["group_id"], unique = false)
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
data class DbVppIdGroupCrossover(
    @ColumnInfo(name = "vpp_id") val vppId: Int,
    @ColumnInfo(name = "group_id") val groupId: Int,
)
