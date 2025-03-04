package plus.vplan.app.domain.repository.schulverwalter

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.repository.WebEntityRepository

interface GradeRepository: WebEntityRepository<Grade> {
    suspend fun download(): Response<Set<Int>>
    suspend fun setConsiderForFinalGrade(gradeId: Int, useForFinalGrade: Boolean)
}