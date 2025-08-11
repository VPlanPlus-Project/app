package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "fk_news_school",
    primaryKeys = ["school_id", "news_id"],
    indices = [
        Index("school_id", unique = false),
        Index("news_id", unique = false)
    ]
)
data class FKNewsSchool(
    @ColumnInfo(name = "school_id") val schoolId: Uuid,
    @ColumnInfo(name = "news_id") val newsId: Int
)