package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbWeek
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.WeekRepository

class WeekRepositoryImpl(
    private val vppDatabase: VppDatabase
) : WeekRepository {
    override suspend fun upsert(week: Week) {
        upsert(listOf(week))
    }

    override suspend fun upsert(weeks: List<Week>) {
        vppDatabase.weekDao.upsert(weeks.map { week ->
            DbWeek(
                id = week.id,
                schoolId = week.school.getItemId().toInt(),
                calendarWeek = week.calendarWeek,
                start = week.start,
                end = week.end,
                weekType = week.weekType,
                weekIndex = week.weekIndex
            )
        })
    }

    override fun getBySchool(schoolId: Int): Flow<List<Week>> {
        return vppDatabase.weekDao
            .getBySchool(schoolId).map { it.map { embeddedWeek -> embeddedWeek.toModel() } }
    }

    override fun getById(id: String): Flow<Week?> {
        return vppDatabase.weekDao.getById(id).map { it?.toModel() }
    }

    override suspend fun deleteBySchool(schoolId: Int) {
        vppDatabase.weekDao.deleteBySchool(schoolId)
    }

    override suspend fun deleteById(id: String) {
        vppDatabase.weekDao.deleteById(id)
    }

    override suspend fun deleteById(id: List<String>) {
        vppDatabase.weekDao.deleteById(id)
    }
}