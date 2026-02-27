package plus.vplan.app.core.data.news

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.dao.NewsDao
import plus.vplan.app.core.database.model.database.DbNews
import plus.vplan.app.core.database.model.database.DbNewsSchools
import plus.vplan.app.core.model.News
import plus.vplan.app.core.model.School
import plus.vplan.app.network.vpp.news.NewsApi
import plus.vplan.app.network.vpp.news.NewsDto

class NewsRepositoryImpl(
    private val newsApi: NewsApi,
    private val newsDao: NewsDao,
) : NewsRepository {
    override suspend fun getBySchool(school: School.AppSchool, forceReload: Boolean): Flow<List<News>> {
        return newsDao
            .getAll()
            .map { items ->
                items
                    .map { it.toModel() }
                    .filter { it.schools.intersect(school.aliases).isNotEmpty() }
            }
            .map { news ->
                if (news.isEmpty() || forceReload) {
                    val items = newsApi.getBySchool(school)
                        .map { item -> item.toEntity() }
                        .map { item ->
                            val existing = news.firstOrNull { it.id == item.id }
                            item.copy(isRead = existing?.isRead ?: false)
                        }
                    items.forEach { save(it) }
                    items
                } else news
            }
    }

    override suspend fun getById(id: Int): Flow<News?> {
        return newsDao.getById(id).map { it?.toModel() }
    }

    override suspend fun getAll(): Flow<List<News>> = newsDao.getAll().map { newsList -> newsList.map { it.toModel() } }

    override suspend fun delete(news: List<News>) {
        newsDao.delete(news.map { it.id })
    }

    override suspend fun save(news: News) {
        newsDao.upsert(
            news = listOf(
                DbNews(
                    id = news.id,
                    author = news.author,
                    title = news.title,
                    content = news.content,
                    createdAt = news.date,
                    notBefore = news.dateFrom,
                    notAfter = news.dateTo,
                    notBeforeVersion = news.versionFrom,
                    notAfterVersion = news.versionTo,
                    isRead = news.isRead
                )
            ),
            schools = news.schools.map {  alias ->
                DbNewsSchools(
                    newsId = news.id,
                    value = alias.value,
                    aliasType = alias.provider,
                    version = alias.version,
                )
            }
        )
    }
}
private fun NewsDto.toEntity() = News(
    id = this.id,
    title = this.title,
    content = this.content,
    date = this.date,
    versionFrom = this.versionFrom,
    versionTo = this.versionTo,
    dateFrom = this.dateFrom,
    dateTo = this.dateTo,
    schools = this.schools,
    author = this.author,
    isRead = false
)