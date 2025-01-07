package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_school_group",
    primaryKeys = ["group_id"],
    indices = [
        Index("school_id", unique = false),
        Index("group_id", unique = true)
    ]
)
data class FKSchoolGroup(
    @ColumnInfo("school_id") val schoolId: Int,
    @ColumnInfo("group_id") val groupId: Int
)
