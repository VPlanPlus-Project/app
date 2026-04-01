package plus.vplan.app.feature.main.domain.usecase.setup

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.model.AliasProvider

class DownloadVppSchoolIdentifierUseCase(
    private val schoolRepository: SchoolRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.Default + CoroutineName(this::class.qualifiedName + ".Invoke")) {
            val schools = profileRepository.getAll().first()
                .map { it.school }
                .distinctBy { it.id }

            val schoolsWithoutVppId = schools
                .filter { it.aliases.none { alias -> alias.provider == AliasProvider.Vpp } }

            schoolsWithoutVppId.forEach { school ->
                schoolRepository.getById(
                    identifier = school.aliases.first(),
                    forceReload = true,
                ).first()
            }
        }
    }
}