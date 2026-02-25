package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbNews
import plus.vplan.app.core.database.model.database.DbNewsSchools
import plus.vplan.app.core.model.News

data class EmbeddedNews(
    @Embedded val news: DbNews,
    @Relation(
        parentColumn = "id",
        entityColumn = "news_id",
        entity = DbNewsSchools::class
    ) val schools: List<DbNewsSchools>
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
            schools = schools.map { it.toModel() }.toSet(),
            author = news.author,
            isRead = news.isRead
        )
    }
}