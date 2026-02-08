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
import kotlinx.datetime.LocalDate
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.data.source.database.model.database.DbHoliday
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Holiday
import plus.vplan.app.domain.repository.DayRepository
import kotlin.uuid.Uuid

class DayRepositoryImpl(
    private val vppDatabase: VppDatabase
) : DayRepository {

    private val getBySchoolDateFlows = mutableMapOf<Pair<LocalDate, Uuid>, Flow<Day?>>()
    private val getBySchoolFlows = mutableMapOf<Uuid, Flow<Set<Day>>>()
    private val getHolidaysFlows = mutableMapOf<Uuid, Flow<List<Holiday>>>()
    override suspend fun insert(day: Day) {
        vppDatabase.dayDao.upsert(DbDay(
            id = day.id,
            date = day.date,
            info = day.info,
            weekId = day.weekId,
            schoolId = day.schoolId
        ))
    }

    override suspend fun upsert(day: Holiday) {
        upsert(listOf(day))
    }

    override suspend fun upsert(holidays: List<Holiday>) {
        vppDatabase.holidayDao.upsert(holidays.map { holiday ->
            DbHoliday(
                id = holiday.id,
                date = holiday.date,
                schoolId = holiday.school
            )
        })
    }

    override suspend fun getHolidays(schoolId: Uuid): Flow<List<Holiday>> {
        return getHolidaysFlows.getOrPut(schoolId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.holidayDao.getBySchoolId(schoolId).map { holidays ->
                holidays.map { it.toModel() }
            }.shareIn(
                upstreamScope,
                started = SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = 5_000,
                    replayExpirationMillis = Long.MAX_VALUE
                ),
                replay = 1
            )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                getHolidaysFlows.remove(schoolId)
            }

            shared
        }
    }

    override suspend fun deleteHolidayById(id: String) {
        deleteHolidaysByIds(listOf(id))
    }

    override suspend fun deleteHolidaysByIds(ids: List<String>) {
        vppDatabase.holidayDao.deleteByIds(ids)
    }

    override fun getBySchool(date: LocalDate, schoolId: Uuid): Flow<Day?> {
        val key = date to schoolId
        return getBySchoolDateFlows.getOrPut(key) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.dayDao.getBySchool(date, schoolId).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                getBySchoolDateFlows.remove(key)
            }

            shared
        }
    }

    override fun getBySchool(schoolId: Uuid): Flow<Set<Day>> {
        return getBySchoolFlows.getOrPut(schoolId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.dayDao.getBySchool(schoolId).map { it.map { day -> day.toModel() }.toSet() }
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

    override fun getById(id: String): Flow<Day?> {
        return getBySchool(LocalDate.parse(id.substringAfter("/")), Uuid.parseHex(id.substringBefore("/")))
    }
}