package plus.vplan.app.feature.settings.page.school.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import plus.vplan.app.domain.repository.ProfileRepository

class GetSchoolsUseCase(
    private val profileRepository: ProfileRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke() = profileRepository.getAll().flatMapLatest { profiles ->
        flowOf(profiles.map { it.school })
    }
}