package plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase

import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.utils.removeFollowingDuplicates

class SearchForSchoolUseCase(
    private val schoolRepository: SchoolRepository
) {
    private val onlineSchools = mutableListOf<OnboardingSchoolOption>()

    suspend fun init(): Response.Error? {
        val response = schoolRepository.downloadSchools()
        if (response is Response.Success) {
            onlineSchools.clear()
            onlineSchools.addAll(
                response
                    .data
                    .sortedBy { it.name }
                    .map { school ->
                        OnboardingSchoolOption(
                            id = school.id,
                            name = school.name,
                            sp24Id = school.aliases.firstOrNull { it.provider == AliasProvider.Sp24 }?.value?.toInt()
                        )
                    }
            )
            return null
        }
        return response as Response.Error
    }

    suspend operator fun invoke(query: String): Response<List<OnboardingSchoolOption>> {
        if (onlineSchools.isEmpty()) {
            val response = init()
            if (response != null) return response
        }
        if (query.isBlank()) return Response.Success(emptyList())
        val adjustedQuery = query.optimizeForSearch()
        return Response.Success(
            onlineSchools
                .filter {
                    it.searchOptimizedName.contains(adjustedQuery) ||
                            it.searchOptimizedName.split(" ").joinToString("") { wordPart -> wordPart.first().toString() }.contains(adjustedQuery) ||
                            it.sp24Id?.toString()?.startsWith(adjustedQuery) == true
                }
        )
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