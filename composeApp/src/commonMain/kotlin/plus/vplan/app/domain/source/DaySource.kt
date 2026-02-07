@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.App
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMap
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMapFactory

class DaySource : KoinComponent {
    private val dayRepository: DayRepository by inject()
    private val weekRepository: WeekRepository by inject()
    private val timetableRepository: TimetableRepository by inject()
    private val substitutionPlanRepository: SubstitutionPlanRepository by inject()
    private val assessmentRepository: AssessmentRepository by inject()
    private val homeworkRepository: HomeworkRepository by inject()
    private val concurrentHashMapFactory: ConcurrentHashMapFactory by inject()

    private val flows: ConcurrentHashMap<String, MutableSharedFlow<CacheState<Day>>> = concurrentHashMapFactory.create()

    fun findNextRegularSchoolDayAfter(
        holidays: List<LocalDate>,
        weeks: List<Week>,
        after: LocalDate,
        dayOfWeeks: Int?
    ): LocalDate? {
        if (holidays.isEmpty()) return null
        var nextSchoolDay = after + 1.days
        val currentWeek = weeks.firstOrNull { nextSchoolDay in it.start..it.end.atStartOfWeek().plus(7.days) }
        if (currentWeek == null && weeks.any { it.start > nextSchoolDay }) {
            nextSchoolDay = weeks.first { it.start > nextSchoolDay }.start
        }

        while (holidays.maxOf { it } > nextSchoolDay && !(holidays.none { it == nextSchoolDay } && nextSchoolDay.dayOfWeek.isoDayNumber <= (dayOfWeeks ?: 5))) {
            nextSchoolDay += 1.days
        }
        return nextSchoolDay
    }

    fun getById(id: String, contextProfile: Profile? = null): Flow<CacheState<Day>> {
        return channelFlow {
            flows.getOrPut(id) {
                val flow = MutableSharedFlow<CacheState<Day>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                CoroutineScope(Dispatchers.IO).launch {
                    val schoolId = Uuid.parse(id.substringBefore("/"))
                    val date = LocalDate.parse(id.substringAfter("/"))
                    val school = App.schoolSource.getById(schoolId).filterIsInstance<AliasState.Done<School>>().firstOrNull()?.data ?: return@launch
                    val weeks = weekRepository.getBySchool(schoolId).first()
                    val day = MutableStateFlow(
                        Day(
                            id = Day.buildId(school, date),
                            date = date,
                            schoolId = schoolId,
                            weekId = weeks.firstOrNull { date in it.start..it.end }?.id,
                            info = null,
                            dayType = Day.DayType.UNKNOWN,
                            timetable = emptySet(),
                            substitutionPlan = emptySet(),
                            assessmentIds = emptySet(),
                            homeworkIds = emptySet(),
                            nextSchoolDayId = null,
                            tags = emptySet()
                        )
                    )

                    launch {
                        day.collect {
                            flow.tryEmit(CacheState.Done(it))
                        }
                    }

                    launch {
                        combine(
                            weekRepository.getBySchool(schoolId).distinctUntilChanged(),
                            dayRepository.getHolidays(schoolId).map { it.map { holiday -> holiday.date } }.distinctUntilChanged(),
                            dayRepository.getBySchool(date, schoolId).distinctUntilChanged()
                        ) { weeks, holidays, dayInfo ->
                            val dayWeek = weeks.firstOrNull { date in it.start..it.end }
                            day.update {
                                it.copy(
                                    weekId = dayWeek?.id,
                                    info = dayInfo?.info,
                                    dayType =
                                        if (date in holidays) Day.DayType.HOLIDAY
                                        else if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                                            var friday = date
                                            while (friday.dayOfWeek != DayOfWeek.FRIDAY) {
                                                friday -= 1.days
                                            }
                                            var monday = date
                                            while (monday.dayOfWeek != DayOfWeek.MONDAY) {
                                                monday += 1.days
                                            }
                                            if (holidays.any { it == friday } || holidays.any { it == monday }) Day.DayType.HOLIDAY else Day.DayType.WEEKEND
                                        } else Day.DayType.REGULAR,
                                    tags = it.tags + Day.DayTags.HAS_METADATA
                                )
                            }
                        }
                            .collect()
                    }

                    launch {
                        (if (contextProfile == null) substitutionPlanRepository.getSubstitutionPlanBySchool(schoolId, date)
                        else substitutionPlanRepository.getForProfile(contextProfile, date)).collectLatest { substitutionPlanLessonIds ->
                            day.update {
                                it.copy(
                                    substitutionPlan = substitutionPlanLessonIds,
                                    tags = it.tags + Day.DayTags.HAS_LESSONS
                                )
                            }
                        }
                    }

                    launch {
                        val week = day.value.week?.getFirstValueOld()
                        val weekIndex = week?.weekIndex ?: -1

                        (if (contextProfile == null) timetableRepository.getForSchool(schoolId, weekIndex, dayOfWeek = date.dayOfWeek)
                        else timetableRepository.getForProfile(contextProfile, weekIndex, dayOfWeek = date.dayOfWeek)).collectLatest { timetableLessonIds ->
                            day.update {
                                it.copy(
                                    timetable = timetableLessonIds,
                                    tags = it.tags + Day.DayTags.HAS_LESSONS
                                )
                            }
                        }
                    }

                    launch {
                        (if (contextProfile == null) assessmentRepository.getByDate(date).map { assessments -> assessments.map { it.id }.toSet() }
                        else assessmentRepository.getByProfile(contextProfile.id, date).map { assessments -> assessments.map { it.id }.toSet() }).collectLatest { assessmentIds ->
                            day.update {
                                it.copy(
                                    assessmentIds = assessmentIds
                                )
                            }
                        }
                    }

                    launch {
                        (if (contextProfile == null) homeworkRepository.getByDate(date).map { homework -> homework.map { it.id }.toSet() }
                        else homeworkRepository.getByProfile(contextProfile.id, date).map { homework -> homework.map { it.id }.toSet() }).collectLatest { homeworkIds ->
                            day.update {
                                it.copy(
                                    homeworkIds = homeworkIds
                                )
                            }
                        }
                    }

                    launch {
                        combine(
                            dayRepository.getHolidays(schoolId).map { it.map { holiday -> holiday.date } },
                            weekRepository.getBySchool(schoolId)
                        ) { holidays, weeks ->
                            val nextSchoolDay = findNextRegularSchoolDayAfter(holidays, weeks, date, (school as? School.AppSchool)?.daysPerWeek)
                            day.update {
                                it.copy(
                                    nextSchoolDayId = nextSchoolDay?.let { Day.buildId(school, nextSchoolDay) },
                                )
                            }
                        }.collect()
                    }
                }
                return@getOrPut flow
            }.collect { send(it) }
        }
    }
}
