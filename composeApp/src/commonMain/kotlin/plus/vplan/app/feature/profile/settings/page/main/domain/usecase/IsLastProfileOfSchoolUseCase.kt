package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.ProfileRepository

class IsLastProfileOfSchoolUseCase(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(profile: Profile) = profileRepository.getAll().map { it.count { p -> p.school.id == profile.school.id } == 1 }
}