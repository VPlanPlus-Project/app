package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import kotlin.uuid.Uuid

@Entity(
    tableName = "courses_aliases",
    primaryKeys = ["alias", "alias_type", "course_id", "version"],
    indices = [
        Index(value = ["alias", "alias_type", "course_id"], unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbCourse::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbCourseAlias(
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "alias_type") val aliasType: AliasProvider,
    @ColumnInfo(name = "course_id") val id: Uuid,
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
        fun fromAlias(alias: Alias, id: Uuid) = DbCourseAlias(
            alias = alias.value,
            aliasType = alias.provider,
            id = id,
            version = alias.version
        )
    }
}