package plus.vplan.app.domain.repository

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.SchoolApiAccess

interface AssessmentRepository {
    suspend fun download(
        schoolApiAccess: SchoolApiAccess,
        defaultLessonIds: List<String>
    ): Response.Error?
}