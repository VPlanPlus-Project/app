package plus.vplan.app.feature.home.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.repository.NewsRepository
import plus.vplan.app.domain.repository.ProfileRepository

class LoadNewsUseCase(
    private val profileRepository: ProfileRepository,
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke() {
        profileRepository.getAll().first()
            .mapNotNull { it.getSchool().getFirstValue() }
            .distinctBy { it.id }
            .forEach { school ->
                newsRepository.download(school.getSchoolApiAccess() ?: return@forEach)
            }
    }
}