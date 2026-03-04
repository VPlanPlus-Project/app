package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.model.Profile

class DeleteProfileUseCase(
    private val profileRepository: ProfileRepository,
    private val keyValueRepository: KeyValueRepository
) {
    private val logger = Logger.withTag("DeleteProfileUseCase")
    suspend operator fun invoke(profile: Profile) {
        val existingProfiles = profileRepository.getAll().first().filter { it.id != profile.id }

        if (existingProfiles.isEmpty()) {
            logger.i { "Need to go to onboarding after deletion" }
        }

        (existingProfiles.firstOrNull { it.school.id == profile.school.id } ?: existingProfiles.firstOrNull())?.let { nextProfile ->
            keyValueRepository.set(Keys.CURRENT_PROFILE, nextProfile.id.toHexString())
        } ?: keyValueRepository.delete(Keys.CURRENT_PROFILE)

        profileRepository.delete(profile)
    }
}