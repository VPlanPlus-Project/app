package plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.OnlineSchool
import plus.vplan.app.domain.repository.SchoolRepository

class SearchForSchoolUseCase(
    private val schoolRepository: SchoolRepository
) {
    private val onlineSchools = mutableListOf<SearchSchool>()

    suspend fun init() {
        val response = schoolRepository.fetchAllOnline()
        if (response is Response.Success) {
            onlineSchools.clear()
            onlineSchools.addAll(
                response
                    .data
                    .sortedBy { it.name }
                    .map { SearchSchool(it) }
            )
        }
    }

    suspend operator fun invoke(query: String): Response<List<OnlineSchool>> {
        if (onlineSchools.isEmpty()) {
            init()
        }
        if (query.isBlank()) return Response.Success(emptyList())
        val adjustedQuery = query
            .lowercase()
            .replace("-", " ")
            .trim()
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
    constructor(school: OnlineSchool) : this(
        school,
        school.name.lowercase().replace("-", " ").trim()
    )
}