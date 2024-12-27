package plus.vplan.app.feature.profile.page.domain.usecase

import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.repository.ProfileRepository

class GetProfilesUseCase(
    private val profileRepository: ProfileRepository
) {

    operator fun invoke() = profileRepository.getAll().map { profiles ->
        profiles
            .groupBy { it.school }
            .mapValues { profilesBySchool ->
                profilesBySchool.value.sortedBy { it.profileType.ordinal.toString() + it.customName }
            }
    }
}