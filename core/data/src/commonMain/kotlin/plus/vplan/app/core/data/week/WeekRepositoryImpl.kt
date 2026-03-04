package plus.vplan.app.core.data.week

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import plus.vplan.app.core.database.dao.WeekDao
import plus.vplan.app.core.database.model.database.DbWeek
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Week
import kotlin.uuid.Uuid

class WeekRepositoryImpl(
    private val weekDao: WeekDao,
    private val applicationScope: CoroutineScope,
) : WeekRepository {

    private val bySchoolCache = mutableMapOf<Uuid, Flow<List<Week>>>()

    override suspend fun save(weeks: List<Week>) {
        weekDao.upsert(weeks.map { week ->
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

    override fun getBySchool(school: School): Flow<List<Week>> {
        return bySchoolCache.getOrPut(school.id) {
            weekDao
                .getBySchool(school.id)
                .map { it.map { embeddedWeek -> embeddedWeek.toModel() } }
                .distinctUntilChanged()
                .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
        }
    }

    override fun getById(id: String): Flow<Week?> {
        return weekDao.getById(id)
            .map { it?.toModel() }
            .distinctUntilChanged()
    }

    override suspend fun delete(weeks: List<Week>) {
        weekDao.deleteById(weeks.map { it.id })
    }
}