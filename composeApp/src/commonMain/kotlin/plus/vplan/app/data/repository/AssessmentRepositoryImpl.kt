package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.repository.AssessmentRepository

class AssessmentRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : AssessmentRepository {
    override suspend fun download(schoolApiAccess: SchoolApiAccess, defaultLessonIds: List<String>): Response.Error? {
        TODO("Not yet implemented")
    }
}