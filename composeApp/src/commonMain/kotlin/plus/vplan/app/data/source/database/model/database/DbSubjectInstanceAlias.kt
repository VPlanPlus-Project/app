package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import kotlin.uuid.Uuid

@Entity(
    tableName = "subject_instances_aliases",
    primaryKeys = ["alias", "alias_type", "subject_instance_id", "version"],
    indices = [
        Index(value = ["alias", "alias_type", "subject_instance_id"], unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbSubjectInstance::class,
            parentColumns = ["id"],
            childColumns = ["subject_instance_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbSubjectInstanceAlias(
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "alias_type") val aliasType: AliasProvider,
    @ColumnInfo(name = "subject_instance_id") val id: Uuid,
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
        fun fromAlias(alias: Alias, id: Uuid) = DbSubjectInstanceAlias(
            alias = alias.value,
            aliasType = alias.provider,
            id = id,
            version = alias.version
        )
    }
}