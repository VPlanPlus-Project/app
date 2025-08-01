package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import kotlin.uuid.Uuid

@Entity(
    tableName = "teachers_aliases",
    primaryKeys = ["alias", "alias_type", "teacher_id", "version"],
    indices = [
        Index(value = ["alias", "alias_type",  "teacher_id"], unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbTeacher::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbTeacherAlias(
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "alias_type") val aliasType: AliasProvider,
    @ColumnInfo(name = "teacher_id") val id: Uuid,
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
        fun fromAlias(alias: Alias, id: Uuid) = DbTeacherAlias(
            alias = alias.value,
            aliasType = alias.provider,
            id = id,
            version = alias.version
        )
    }
}