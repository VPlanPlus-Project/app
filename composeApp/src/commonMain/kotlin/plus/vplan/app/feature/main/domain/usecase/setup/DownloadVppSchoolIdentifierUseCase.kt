package plus.vplan.app.feature.main.domain.usecase.setup

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.model.AliasProvider

class DownloadVppSchoolIdentifierUseCase(
    private val schoolRepository: SchoolRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke() {
        val schools = profileRepository.getAll().first()
            .map { it.school }
            .distinctBy { it.id }

        val schoolsWithoutVppId = schools
            .filter { it.aliases.none { alias -> alias.provider == AliasProvider.Vpp } }

        schoolsWithoutVppId.forEach { school ->
            schoolRepository.getById(school.aliases.first()).first()
        }
    }
}