@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Entity(
    tableName = "news",
    primaryKeys = ["id"],
    indices = [
        Index("id", unique = true)
    ]
)
data class DbNews(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "not_before") val notBefore: Instant,
    @ColumnInfo(name = "not_after") val notAfter: Instant,
    @ColumnInfo(name = "not_before_version") val notBeforeVersion: Int?,
    @ColumnInfo(name = "not_after_version") val notAfterVersion: Int?,
    @ColumnInfo(name = "is_read") val isRead: Boolean
)
