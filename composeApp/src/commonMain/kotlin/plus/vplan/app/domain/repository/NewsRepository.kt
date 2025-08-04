package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.News
import plus.vplan.app.domain.model.School

interface NewsRepository: WebEntityRepository<News> {
    suspend fun download(school: School.AppSchool): Response<List<Int>>
    suspend fun getAll(): Flow<List<News>>
    suspend fun delete(ids: List<Int>)
    suspend fun upsert(news: News)
}