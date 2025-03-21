package plus.vplan.app.feature.sync.domain.usecase.vpp

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.NewsRepository
import plus.vplan.app.domain.repository.ProfileRepository

class UpdateNewsUseCase(
    private val newsRepository: NewsRepository,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke() {
        val existing = newsRepository.getAll().first()
        val downloadedNewsIds = mutableSetOf<Int>()
        profileRepository.getAll().first()
            .mapNotNull { it.getSchool().getFirstValue() }
            .distinctBy { it.id }
            .forEach { school ->
                val response = newsRepository.download(school.getSchoolApiAccess()!!)
                if (response is Response.Success) {
                    downloadedNewsIds.addAll(response.data)
                }
            }

        val newsToDelete = existing.map { it.id }.toSet() - downloadedNewsIds
        newsRepository.delete(newsToDelete.toList())
    }
}