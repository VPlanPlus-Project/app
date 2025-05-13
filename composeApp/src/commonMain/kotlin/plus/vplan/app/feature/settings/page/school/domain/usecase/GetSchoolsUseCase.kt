package plus.vplan.app.feature.settings.page.school.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.ProfileRepository

class GetSchoolsUseCase(
    private val profileRepository: ProfileRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke() = profileRepository.getAll().flatMapLatest { profiles ->
        if (profiles.isEmpty()) return@flatMapLatest flowOf(emptyList())
        else combine(profiles.map { profile -> profile.getSchool().filterIsInstance<CacheState.Done<School>>().map { it.data } }) { it.toList().distinctBy { school -> school.id } }
    }
}