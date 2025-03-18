package plus.vplan.app.domain.repository

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.News
import plus.vplan.app.domain.model.SchoolApiAccess

interface NewsRepository: WebEntityRepository<News> {
    suspend fun getBySchool(schoolApiAccess: SchoolApiAccess, reload: Boolean = false): Response<List<News>>
}