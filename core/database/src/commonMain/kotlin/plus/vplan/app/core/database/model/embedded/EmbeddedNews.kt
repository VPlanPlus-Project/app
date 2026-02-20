package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbNews
import plus.vplan.app.core.database.model.database.foreign_key.FKNewsSchool
import plus.vplan.app.core.model.News

data class EmbeddedNews(
    @Embedded val news: DbNews,
    @Relation(
        parentColumn = "id",
        entityColumn = "news_id",
        entity = FKNewsSchool::class
    ) val schools: List<FKNewsSchool>
) {
    fun toModel(): News {
        return News(
            id = news.id,
            title = news.title,
            content = news.content,
            date = news.createdAt,
            versionFrom = news.notBeforeVersion,
            versionTo = news.notAfterVersion,
            dateFrom = news.notBefore,
            dateTo = news.notAfter,
            schoolIds = schools.map { it.schoolId },
            author = news.author,
            isRead = news.isRead
        )
    }
}