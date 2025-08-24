package plus.vplan.app.feature.main.domain.usecase.setup

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.SchoolDbDto
import plus.vplan.app.domain.repository.SchoolRepository

class DownloadVppSchoolIdentifierUseCase(
    private val schoolRepository: SchoolRepository
) {
    suspend operator fun invoke() {
        val schoolsWithoutVppId = schoolRepository.getAllLocalIds().first()
            .mapNotNull { schoolRepository.getByLocalId(it).first() }
            .filter { it.aliases.none { alias -> alias.provider == AliasProvider.Vpp } }

        schoolsWithoutVppId.forEach { school ->
            val downloadedItem = schoolRepository.downloadById(school.aliases.first().toUrlString())
            if (downloadedItem !is Response.Success) return@forEach
            schoolRepository.upsert(SchoolDbDto(
                name = school.name,
                aliases = (school.aliases + downloadedItem.data.aliases).toList().distinctBy { "${it.provider.name}_${it.version}" },
                creationReason = CreationReason.Cached
            ))
        }
    }
}