package plus.vplan.app.domain.repository.schulverwalter

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.repository.WebEntityRepository

interface IntervalRepository: WebEntityRepository<Interval> {
    suspend fun download(): Response<Set<Int>>
}