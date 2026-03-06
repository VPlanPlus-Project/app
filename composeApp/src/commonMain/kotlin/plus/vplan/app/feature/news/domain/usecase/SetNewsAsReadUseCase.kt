package plus.vplan.app.feature.news.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.data.news.NewsRepository

class SetNewsAsReadUseCase(
    private val newsRepository: NewsRepository,
    private val analyticsRepository: AnalyticsRepository,
) {
    suspend operator fun invoke(newsId: Int) {
        val news = newsRepository.getById(newsId).first() ?: return
        if (!news.isRead) analyticsRepository.capture("News.Read", mapOf("news_id" to newsId))
        newsRepository.save(news.copy(isRead = true))
    }
}
