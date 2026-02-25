package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider

@Entity(
    tableName = "news_schools",
    primaryKeys = ["news_id", "value", "provider", "version"],
    foreignKeys = [
        ForeignKey(
            entity = DbNews::class,
            parentColumns = ["id"],
            childColumns = ["news_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbNewsSchools(
    @ColumnInfo("news_id") val newsId: Int,
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "provider") val aliasType: AliasProvider,
    @ColumnInfo(name = "version") val version: Int
) {
    fun toModel(): Alias {
        return Alias(
            provider = aliasType,
            value = value,
            version = version,
        )
    }
}