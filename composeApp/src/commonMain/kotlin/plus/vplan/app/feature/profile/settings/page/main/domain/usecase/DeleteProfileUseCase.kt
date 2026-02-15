package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.SchoolRepository

class DeleteProfileUseCase(
    private val profileRepository: ProfileRepository,
    private val schoolRepository: SchoolRepository,
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

        if (existingProfiles.none { it.school.id == profile.school.id }) {
            logger.i { "Deleting school ${profile.school.name} (${profile.school.id})" }
            schoolRepository.deleteSchool(profile.school.id) // deletes profile as well
        } else {
            profileRepository.deleteProfile(profileId = profile.id)
        }
    }
}