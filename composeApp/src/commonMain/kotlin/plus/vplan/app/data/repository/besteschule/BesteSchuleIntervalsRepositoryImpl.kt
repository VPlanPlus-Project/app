package plus.vplan.app.data.repository.besteschule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteSchuleInterval
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleIntervalUser
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository

class BesteSchuleIntervalsRepositoryImpl : BesteSchuleIntervalsRepository, KoinComponent {
    private val vppDatabase by inject<VppDatabase>()

    private val cacheFlows = mutableMapOf<Int, Flow<BesteSchuleInterval?>>()

    override suspend fun addIntervalsToCache(intervals: Set<BesteSchuleInterval>) = withContext(Dispatchers.IO) {
        val existingYears = vppDatabase.besteSchuleYearDao.getAll().first().map { it.besteSchuleYear.id }.toSet()
        val existingIntervals = vppDatabase.besteSchuleIntervalDao.getAll().first().map { it.interval.id }
            .toSet() + intervals.map { it.id }

        val intervals = intervals
            .filter { it.yearId in existingYears }
            .filter { it.includedIntervalId == null || it.includedIntervalId in existingIntervals }

        vppDatabase.besteSchuleIntervalDao.upsert(intervals.map { interval ->
            DbBesteSchuleInterval(
                id = interval.id,
                name = interval.name,
                type = interval.type.name,
                from = interval.from,
                to = interval.to,
                includedIntervalId = interval.includedIntervalId,
                yearId = interval.yearId,
                cachedAt = interval.cachedAt
            )
        })

        vppDatabase.besteSchuleIntervalDao.upsertUserMappings(intervals.flatMap { interval ->
            interval.linkedToSchulverwalterAccountIds.map { accountId ->
                DbBesteschuleIntervalUser(
                    intervalId = interval.id,
                    schulverwalterUserId = accountId
                )
            }
        })
    }

    override fun getIntervalFromCache(intervalId: Int): Flow<BesteSchuleInterval?> {
        return cacheFlows.getOrPut(intervalId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.besteSchuleIntervalDao.getById(intervalId).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                cacheFlows.remove(intervalId)
            }

            shared
        }
    }

    override fun getIntervalsFromCache(userId: Int): Flow<List<BesteSchuleInterval>> {
        return vppDatabase.besteSchuleIntervalDao.getIntervalsForUser(userId)
            .map { it.map { it.toModel() } }
    }
}