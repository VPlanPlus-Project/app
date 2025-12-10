package plus.vplan.app.domain.repository.schulverwalter

import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.repository.WebEntityRepository

interface IntervalRepository: WebEntityRepository<Interval> {

    /**
     * Replaces the connections for a user
     */
    suspend fun connectIntervalsWithSchulverwalterUserId(schulverwalterUserId: Int, intervalIds: Set<Int>)
}