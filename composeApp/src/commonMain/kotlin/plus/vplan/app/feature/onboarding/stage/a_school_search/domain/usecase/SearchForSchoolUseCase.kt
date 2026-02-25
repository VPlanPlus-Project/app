package plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase

import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.utils.removeFollowingDuplicates

class SearchForSchoolUseCase(
    private val schoolRepository: SchoolRepository,
) {
    private val onlineSchools = mutableListOf<OnboardingSchoolOption>()

    suspend fun init() {
        val response = schoolRepository.getAll()
        onlineSchools.clear()
        onlineSchools.addAll(
            response
                .sortedBy { it.name }
                .map { school ->
                    OnboardingSchoolOption(
                        id = school.aliases.first { it.provider == AliasProvider.Vpp }.value.toInt(),
                        name = school.name,
                        sp24Id = school.aliases.firstOrNull { it.provider == AliasProvider.Sp24 }?.value?.toInt()
                    )
                }
        )
    }

    suspend operator fun invoke(query: String): List<OnboardingSchoolOption> {
        if (onlineSchools.isEmpty()) init()
        if (query.isBlank()) return emptyList()
        val adjustedQuery = query.optimizeForSearch()
        return onlineSchools
            .filter {
                it.searchOptimizedName.contains(adjustedQuery) ||
                        it.searchOptimizedName.split(" ").joinToString("") { wordPart -> wordPart.first().toString() }.contains(adjustedQuery) ||
                        it.sp24Id?.toString()?.startsWith(adjustedQuery) == true
            }
    }
}

private fun String.optimizeForSearch(): String {
    return this.lowercase()
        .replace("-", " ")
        .replace(".", " ")
        .replace("'", " ")
        .replace("\"", " ")
        .removeFollowingDuplicates(' ')
        .trim()
}

data class OnboardingSchoolOption(
    val id: Int,
    val name: String,
    val sp24Id: Int?,
    val searchOptimizedName: String = name.optimizeForSearch()
)