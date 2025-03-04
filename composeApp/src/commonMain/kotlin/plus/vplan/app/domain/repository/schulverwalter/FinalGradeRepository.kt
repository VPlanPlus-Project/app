package plus.vplan.app.domain.repository.schulverwalter

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.FinalGrade
import plus.vplan.app.domain.repository.WebEntityRepository

interface FinalGradeRepository : WebEntityRepository<FinalGrade> {
    suspend fun download(): Response<Set<Int>>
}