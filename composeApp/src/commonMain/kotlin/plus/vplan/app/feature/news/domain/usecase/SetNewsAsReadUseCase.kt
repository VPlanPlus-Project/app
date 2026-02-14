package plus.vplan.app.feature.news.domain.usecase

import plus.vplan.app.capture
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.domain.repository.NewsRepository

class SetNewsAsReadUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(newsId: Int) {
        val news = newsRepository.getById(newsId, false).getFirstValueOld() ?: return
        if (!news.isRead) capture("News.Read", mapOf("news_id" to newsId))
        newsRepository.upsert(news.copy(isRead = true))
    }
}