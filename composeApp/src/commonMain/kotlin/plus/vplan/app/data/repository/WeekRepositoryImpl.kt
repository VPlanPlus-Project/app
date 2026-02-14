package plus.vplan.app.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbWeek
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.WeekRepository
import kotlin.uuid.Uuid

class WeekRepositoryImpl(
    private val vppDatabase: VppDatabase
) : WeekRepository {

    private val getBySchoolFlows = mutableMapOf<Uuid, Flow<List<Week>>>()
    private val getByIdFlows = mutableMapOf<String, Flow<Week?>>()

    override suspend fun upsert(week: Week) {
        upsert(listOf(week))
    }

    override suspend fun upsert(weeks: List<Week>) {
        vppDatabase.weekDao.upsert(weeks.map { week ->
            DbWeek(
                id = week.id,
                schoolId = week.school,
                calendarWeek = week.calendarWeek,
                start = week.start,
                end = week.end,
                weekType = week.weekType,
                weekIndex = week.weekIndex
            )
        })
    }

    override fun getBySchool(schoolId: Uuid): Flow<List<Week>> {
        return getBySchoolFlows.getOrPut(schoolId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.weekDao
                .getBySchool(schoolId).map { it.map { embeddedWeek -> embeddedWeek.toModel() } }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                getBySchoolFlows.remove(schoolId)
            }

            shared
        }
    }

    override fun getById(id: String): Flow<Week?> {
        return getByIdFlows.getOrPut(id) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.weekDao.getById(id).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                getByIdFlows.remove(id)
            }

            shared
        }
    }

    override suspend fun deleteBySchool(schoolId: Uuid) {
        vppDatabase.weekDao.deleteBySchool(schoolId)
    }

    override suspend fun deleteById(id: String) {
        vppDatabase.weekDao.deleteById(id)
    }

    override suspend fun deleteById(id: List<String>) {
        vppDatabase.weekDao.deleteById(id)
    }
}