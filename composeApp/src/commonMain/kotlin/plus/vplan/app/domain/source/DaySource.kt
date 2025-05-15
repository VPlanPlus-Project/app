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
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days
import kotlin.uuid.ExperimentalUuidApi

class DaySource(
    private val dayRepository: DayRepository,
    private val weekRepository: WeekRepository,
    private val timetableRepository: TimetableRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val assessmentRepository: AssessmentRepository,
    private val homeworkRepository: HomeworkRepository
) {
    val flows = hashMapOf<String, MutableSharedFlow<CacheState<Day>>>()
    fun findNextRegularSchoolDayAfter(
        holidays: List<LocalDate>,
        after: LocalDate,
        dayOfWeeks: Int?
    ): LocalDate? {
        if (holidays.isEmpty()) return null
        var nextSchoolDay = after + 1.days
        while (holidays.maxOf { it } > nextSchoolDay && !(holidays.none { it == nextSchoolDay } && nextSchoolDay.dayOfWeek.isoDayNumber <= (dayOfWeeks ?: 5))) {
            nextSchoolDay += 1.days
        }
        return nextSchoolDay
    }

    fun getById(id: String, contextProfile: Profile? = null): Flow<CacheState<Day>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Day>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                val schoolId = id.substringBefore("/").toInt()
                val date = LocalDate.parse(id.substringAfter("/"))
                val school = App.schoolSource.getById(schoolId).filterIsInstance<CacheState.Done<School>>().firstOrNull()?.data ?: return@launch
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
                        val dayWeek = weeks.firstOrNull { date in it.start..it.end } ?: weeks.last { it.start < date }
                        day.update {
                            it.copy(
                                weekId = dayWeek.id,
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
                    val week = day.value.week?.getFirstValue()
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
                    (if (contextProfile == null) assessmentRepository.getByDate(date).map { assessments -> assessments.map { it.id }.toSet() }.distinctUntilChanged()
                    else assessmentRepository.getByProfile(contextProfile.id, date).map { assessments -> assessments.map { it.id }.toSet() }.distinctUntilChanged()).collectLatest { assessmentIds ->
                        day.update {
                            it.copy(
                                assessmentIds = assessmentIds
                            )
                        }
                    }
                }

                launch {
                    (if (contextProfile == null) homeworkRepository.getByDate(date).map { homework -> homework.map { it.id }.toSet() }.distinctUntilChanged()
                    else homeworkRepository.getByProfile(contextProfile.id, date).map { homework -> homework.map { it.id }.toSet() }.distinctUntilChanged()).collectLatest { homeworkIds ->
                        day.update {
                            it.copy(
                                homeworkIds = homeworkIds
                            )
                        }
                    }
                }

                launch {
                    dayRepository.getHolidays(schoolId).map { it.map { holiday -> holiday.date } }.collectLatest { holidays ->
                        val nextSchoolDay = findNextRegularSchoolDayAfter(holidays, date, (school as? School.IndiwareSchool)?.daysPerWeek)
                        day.update {
                            it.copy(
                                nextSchoolDayId = nextSchoolDay?.let { Day.buildId(school, nextSchoolDay) },
                            )
                        }
                    }
                }
            }
            flow
        }
    }
}
