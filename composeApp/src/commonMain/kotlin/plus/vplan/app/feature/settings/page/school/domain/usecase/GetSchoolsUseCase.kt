package plus.vplan.app.feature.settings.page.school.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import plus.vplan.app.domain.repository.ProfileRepository

class GetSchoolsUseCase(
    private val profileRepository: ProfileRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke() = profileRepository.getAll().mapLatest { profiles ->
        if (profiles.isEmpty()) emptyList()
        else profiles.map { it.school }.distinctBy { it.id }
    }
}