package plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.OnlineSchool
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.utils.removeFollowingDuplicates

class SearchForSchoolUseCase(
    private val schoolRepository: SchoolRepository
) {
    private val onlineSchools = mutableListOf<SearchSchool>()

    suspend fun init(): Response.Error? {
        val response = schoolRepository.fetchAllOnline()
        if (response is Response.Success) {
            onlineSchools.clear()
            onlineSchools.addAll(
                response
                    .data
                    .sortedBy { it.name }
                    .map { SearchSchool(it) }
            )
            return null
        }
        return response as Response.Error
    }

    suspend operator fun invoke(query: String): Response<List<OnlineSchool>> {
        if (onlineSchools.isEmpty()) {
            val response = init()
            if (response != null) return response
        }
        if (query.isBlank()) return Response.Success(emptyList())
        val adjustedQuery = query.optimizeForSearch()
        return Response.Success(
            onlineSchools
                .filter {
                    it.adjustedName.contains(adjustedQuery) ||
                            it.adjustedName.split(" ").joinToString("") { wordPart -> wordPart.first().toString() }.contains(adjustedQuery) ||
                            it.school.sp24Id?.toString()?.startsWith(adjustedQuery) == true
                }
                .map { it.school }
        )
    }
}

private data class SearchSchool(
    val school: OnlineSchool,
    val adjustedName: String
) {
    constructor(school: OnlineSchool) : this(school, school.name.optimizeForSearch())
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