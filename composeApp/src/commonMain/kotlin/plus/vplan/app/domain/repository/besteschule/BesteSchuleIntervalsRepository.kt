package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.model.besteschule.api.ApiStudentData
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleIntervalsRepository {
    suspend fun addIntervalsToCache(intervals: Set<BesteSchuleInterval>)
    fun getIntervalFromCache(intervalId: Int): Flow<BesteSchuleInterval?>
    fun getIntervalsFromCache(userId: Int): Flow<List<BesteSchuleInterval>>
}