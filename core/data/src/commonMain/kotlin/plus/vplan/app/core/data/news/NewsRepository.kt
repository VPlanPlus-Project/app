package plus.vplan.app.core.data.news

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.News
import plus.vplan.app.core.model.School

interface NewsRepository {
    suspend fun getBySchool(school: School.AppSchool, forceReload: Boolean = false): Flow<List<News>>
    suspend fun getById(id: Int): Flow<News?>
    suspend fun getAll(): Flow<List<News>>
    suspend fun delete(news: News) = delete(listOf(news))
    suspend fun delete(news: List<News>)
    suspend fun save(news: News)
}