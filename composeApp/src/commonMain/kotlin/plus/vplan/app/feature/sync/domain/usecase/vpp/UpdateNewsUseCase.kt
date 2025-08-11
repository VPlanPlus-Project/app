package plus.vplan.app.feature.sync.domain.usecase.vpp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.NewsRepository
import plus.vplan.app.domain.repository.ProfileRepository

class UpdateNewsUseCase(
    private val newsRepository: NewsRepository,
    private val profileRepository: ProfileRepository
) {
    private val logger = Logger.withTag("UpdateNewsUseCase")
    suspend operator fun invoke() {
        val existing = newsRepository.getAll().first()
        val downloadedNewsIds = mutableSetOf<Int>()
        profileRepository.getAll().first()
            .mapNotNull { it.getSchool().getFirstValue() }
            .distinctBy { it.id }
            .forEach { school ->
                val response = newsRepository.download(school)
                if (response is Response.Success) {
                    downloadedNewsIds.addAll(response.data)
                } else {
                    logger.e { "Cannot update news for school ${school.name} (${school.id}); error: $response" }
                    return
                }
            }

        val newsToDelete = existing.map { it.id }.toSet() - downloadedNewsIds
        newsRepository.delete(newsToDelete.toList())
    }
}