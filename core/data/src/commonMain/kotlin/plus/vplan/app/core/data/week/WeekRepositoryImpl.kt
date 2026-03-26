package plus.vplan.app.core.data.week

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.database.dao.WeekDao
import plus.vplan.app.core.database.model.database.DbWeek
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Week
import plus.vplan.app.core.utils.date.atStartOfWeek
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.list.takeContinuousBy
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
                .map { weeks ->
                    val today = LocalDate.now()
                    if (weeks.isEmpty()) return@map emptyList()

                    fun getAllWeeksContainingWeek(week: Week): List<Week>? {
                        val firstWeek = weeks
                            .filter { it.start < week.start }
                            .filter { it.weekIndex == 1 }
                            .maxByOrNull { it.start }
                        if (firstWeek == null) return null
                        return weeks
                            .dropWhile { it != firstWeek }
                            .takeContinuousBy { it.weekIndex }
                    }

                    val weekStates = weeks
                        .sortedBy { it.start }
                        .let findWeeksWithMultipleSchoolYears@{ weeks ->
                            if (weeks.count { it.weekIndex == 1 } == 1) weeks
                            else {
                                // There are multiple school years in the app so we need to
                                // find the most appropriate for the given date.

                                // Check if there is a week which is currently ongoing
                                weeks
                                    .firstOrNull { it.start == today.atStartOfWeek() }
                                    ?.let { currentWeek ->
                                        getAllWeeksContainingWeek(currentWeek)?.let {
                                            return@findWeeksWithMultipleSchoolYears it
                                        }
                                    }

                                // Find the next week starting after today
                                weeks
                                    .filter { it.start > today }
                                    .minByOrNull { it.start }
                                    ?.let { nextWeek ->
                                        getAllWeeksContainingWeek(nextWeek)?.let {
                                            return@findWeeksWithMultipleSchoolYears it
                                        }
                                    }

                                return@map emptyList()
                            }
                        }

                    weekStates.sortedBy { it.weekIndex }
                }
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
        }
    }

    override fun getById(id: String): Flow<Week?> {
        return weekDao.getById(id)
            .map { it?.toModel() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    override suspend fun delete(weeks: List<Week>) {
        weekDao.deleteById(weeks.map { it.id })
    }
}