package plus.vplan.app.feature.sync.domain.usecase.vpp

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.news.NewsRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.model.News

class UpdateNewsUseCase(
    private val newsRepository: NewsRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke() {
        val existing = newsRepository.getAll().first()
        val downloadedNews = mutableSetOf<News>()
        profileRepository.getAll().first()
            .map { it.school }
            .distinctBy { it.id }
            .forEach { school ->
                downloadedNews.addAll(newsRepository.getBySchool(school, forceReload = true).first())
            }

        val newsToDelete = existing.filter { existing -> existing.id !in downloadedNews.map { it.id } }
        newsRepository.delete(newsToDelete)
    }
}