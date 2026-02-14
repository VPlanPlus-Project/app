package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbNews
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.foreign_key.FKNewsSchool
import plus.vplan.app.domain.model.News

data class EmbeddedNews(
    @Embedded val news: DbNews,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = DbSchool::class,
        associateBy = Junction(
            value = FKNewsSchool::class,
            parentColumn = "news_id",
            entityColumn = "school_id"
        ),
    ) val schools: List<EmbeddedSchool>
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
            schools = schools.map { it.toModel() },
            author = news.author,
            isRead = news.isRead
        )
    }
}