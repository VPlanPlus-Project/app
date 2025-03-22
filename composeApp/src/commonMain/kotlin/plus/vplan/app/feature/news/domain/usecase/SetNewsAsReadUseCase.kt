package plus.vplan.app.feature.news.domain.usecase

import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.repository.NewsRepository

class SetNewsAsReadUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(newsId: Int) {
        val news = App.newsSource.getById(newsId, false).getFirstValue() ?: return
        newsRepository.upsert(news.copy(isRead = true))
    }
}