package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import kotlin.uuid.Uuid

@Entity(
    tableName = "schools_aliases",
    primaryKeys = ["alias", "alias_type", "school_id", "version"],
    indices = [
        Index(value = ["alias", "alias_type",  "school_id"], unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbSchool::class,
            parentColumns = ["id"],
            childColumns = ["school_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbSchoolAlias(
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "alias_type") val aliasType: AliasProvider,
    @ColumnInfo(name = "school_id") val id: Uuid,
    @ColumnInfo(name = "version") val version: Int
) {
    fun toModel(): Alias {
        return Alias(
            provider = aliasType,
            value = alias,
            version = version,
        )
    }

    companion object {
        fun fromAlias(alias: Alias, id: Uuid) = DbSchoolAlias(
            alias = alias.value,
            aliasType = alias.provider,
            id = id,
            version = alias.version
        )
    }
}