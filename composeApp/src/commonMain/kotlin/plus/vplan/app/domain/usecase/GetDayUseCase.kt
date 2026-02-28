@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.core.data.day.DayRepository
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.School
import plus.vplan.app.domain.model.populated.DayPopulator
import plus.vplan.app.domain.model.populated.PopulatedDay
import plus.vplan.app.domain.model.populated.PopulationContext
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

private val logger = Logger.withTag("GetDayUseCase")

class GetDayUseCase(
    private val dayRepository: DayRepository,
    private val dayPopulator: DayPopulator,
) {
    operator fun invoke(profile: Profile, date: LocalDate): Flow<PopulatedDay> {
        val school: School.AppSchool = profile.school
        logger.d { "[$date] invoke: schoolId=${school.id}" }
        return dayRepository.getBySchool(school, date)
            .onEach { logger.d { "[$date] dayRepository.getBySchool emitted: ${it?.id ?: "null → synthetic day"}" } }
            .map { it ?: Day(
                id = Day.buildId(school.id, date),
                date = date,
                school = school,
                week = null,
                info = null,
                dayType = if (date.dayOfWeek.isoDayNumber > school.daysPerWeek) Day.DayType.WEEKEND else Day.DayType.REGULAR,
                nextSchoolDay = date + 1.days
            ) }
            .distinctUntilChangedBy { "${it.id}|${it.week?.id}|${it.info}|${it.dayType}" }
            .onEach { logger.d { "[$date] after distinctUntilChangedBy: id=${it.id} dayType=${it.dayType} week=${it.week?.id}" } }
            .flatMapLatest { day ->
                logger.d { "[$date] flatMapLatest → calling DayPopulator.populateSingle" }
                dayPopulator.populateSingle(day, PopulationContext.Profile(profile))
            }
            .onEach { populated ->
                logger.d { "[$date] DayPopulator.populateSingle emitted: timetable=${populated.timetable.size} substitution=${populated.substitution.size} homework=${populated.homework.size} assessments=${populated.assessments.size} holiday=${populated.holiday?.date}" }
            }
    }
}