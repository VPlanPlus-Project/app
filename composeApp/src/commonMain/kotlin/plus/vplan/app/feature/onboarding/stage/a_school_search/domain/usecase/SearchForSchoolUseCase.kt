package plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.OnlineSchool
import plus.vplan.app.domain.repository.SchoolRepository

class SearchForSchoolUseCase(
    private val schoolRepository: SchoolRepository
) {
    private val onlineSchools = mutableListOf<OnlineSchool>()

    suspend fun init() {
        val response = schoolRepository.fetchAllOnline()
        if (response is Response.Success) {
            onlineSchools.clear()
            onlineSchools.addAll(response.data.sortedBy { it.name })
        }
    }

    suspend operator fun invoke(query: String): Response<List<OnlineSchool>> {
        if (onlineSchools.isEmpty()) {
            init()
        }
        return Response.Success(onlineSchools.filter { it.name.contains(query, ignoreCase = true) })
    }
}