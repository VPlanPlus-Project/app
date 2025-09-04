package plus.vplan.app.domain.repository.schulverwalter

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Year
import plus.vplan.app.domain.repository.WebEntityRepository

interface YearRepository: WebEntityRepository<Year> {
    suspend fun download(): Response<Set<Int>>
    suspend fun setCurrent(accessToken: String, yearId: Int?): Response<Unit>
}