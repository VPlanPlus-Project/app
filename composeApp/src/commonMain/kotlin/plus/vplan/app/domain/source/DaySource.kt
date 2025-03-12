package plus.vplan.app.domain.source

import co.touchlab.kermit.Logger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
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
import plus.vplan.app.utils.minus
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

class DaySource(
    private val dayRepository: DayRepository,
    private val weekRepository: WeekRepository,
    private val timetableRepository: TimetableRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val assessmentRepository: AssessmentRepository,
    private val homeworkRepository: HomeworkRepository
) {
    val flows = hashMapOf<String, MutableSharedFlow<CacheState<Day>>>()
    fun getById(id: String, contextProfile: Profile? = null): Flow<CacheState<Day>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheState<Day>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            MainScope().launch {
                channelFlow {
                    val schoolId = id.substringBefore("/").toInt()
                    val date = LocalDate.parse(id.substringAfter("/"))
                    val school = App.schoolSource.getById(schoolId).filterIsInstance<CacheState.Done<School>>().firstOrNull()?.data ?: return@channelFlow
                    try {
                        combine(
                            weekRepository.getBySchool(schoolId),
                            dayRepository.getHolidays(schoolId).map { it.map { holiday -> holiday.date } },
                            dayRepository.getBySchool(date, schoolId)
                        ) { weeks, holidays, dayInfo ->
                            val dayWeek = weeks.firstOrNull { date in it.start..it.end } ?: weeks.last { it.start < date }
                            MetaEmitting(
                                dayWeek = dayWeek,
                                dayInfo = dayInfo,
                                holidays = holidays
                            )
                        }.collectLatest { meta ->
                            val findNextRegularSchoolDayAfter: (LocalDate) -> LocalDate? = findNextRegularSchoolDayAfter@{ startDate ->
                                if (meta.holidays.isEmpty()) return@findNextRegularSchoolDayAfter null
                                var nextSchoolDay = startDate + 1.days
                                while (meta.holidays.maxOf { it } > nextSchoolDay && !(meta.holidays.none { it == nextSchoolDay } && nextSchoolDay.dayOfWeek.isoDayNumber <= ((school as? School.IndiwareSchool)?.daysPerWeek ?: 5))) {
                                    nextSchoolDay += 1.days
                                }
                                nextSchoolDay
                            }
                            combine(
                                if (contextProfile == null) timetableRepository.getForSchool(
                                    schoolId = schoolId,
                                    dayOfWeek = date.dayOfWeek,
                                    weekIndex = meta.dayWeek.weekIndex
                                ) else timetableRepository.getForProfile(
                                    profile = contextProfile,
                                    dayOfWeek = date.dayOfWeek,
                                    weekIndex = meta.dayWeek.weekIndex
                                ),
                                substitutionPlanRepository.getSubstitutionPlanBySchool(schoolId, date),
                                assessmentRepository.getByDate(date).map { assessments -> assessments.map { it.id }.toSet() }.distinctUntilChanged(),
                                homeworkRepository.getByDate(date).map { homework -> homework.map { it.id }.toSet() }.distinctUntilChanged()
                            ) { timetable, substitutionPlan, assessments, homework ->
                                send(CacheState.Done(Day(
                                    id = id,
                                    date = date,
                                    school = schoolId,
                                    weekId = meta.dayWeek.id,
                                    info = meta.dayInfo?.info,
                                    dayType =
                                        if (date in meta.holidays) Day.DayType.HOLIDAY
                                        else if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                                            var friday = date
                                            while (friday.dayOfWeek != DayOfWeek.FRIDAY) {
                                                friday -= 1.days
                                            }
                                            var monday = date
                                            while (monday.dayOfWeek != DayOfWeek.MONDAY) {
                                                monday += 1.days
                                            }
                                            if (meta.holidays.any { it == friday } || meta.holidays.any { it == monday }) Day.DayType.HOLIDAY
                                            else Day.DayType.WEEKEND
                                        }
                                        else if (timetable.isNotEmpty()) Day.DayType.REGULAR
                                        else Day.DayType.UNKNOWN,
                                    timetable = timetable,
                                    substitutionPlan = substitutionPlan,
                                    assessmentIds = assessments,
                                    homeworkIds = homework,
                                    nextSchoolDay = findNextRegularSchoolDayAfter(date)?.let { "$schoolId/$it" }
                                )))
                            }.collect()
                        }
                    } catch (e: NoSuchElementException) {
                        Logger.e { "NoSuchElementException: ${e.stackTraceToString()}" }
                    }
                }.collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }
}

private data class MetaEmitting(
    val dayWeek: Week,
    val dayInfo: Day?,
    val holidays: List<LocalDate>
)