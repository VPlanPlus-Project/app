@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.day.DayRepository
import plus.vplan.app.core.data.holiday.HolidayRepository
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.model.populated.DayPopulator
import plus.vplan.app.domain.model.populated.PopulatedDay
import plus.vplan.app.domain.model.populated.PopulationContext
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

class GetDayUseCase(
    private val dayRepository: DayRepository,
    private val dayPopulator: DayPopulator,
) {
    operator fun invoke(profile: Profile, date: LocalDate): Flow<PopulatedDay> {
        return dayRepository.getBySchool(profile.school, date)
            .map { it ?: Day(
                id = Day.buildId(profile.school, date),
                date = date,
                school = profile.school,
                week = null,
                info = null,
                dayType = Day.DayType.REGULAR,
                nextSchoolDay = date + 1.days
            ) }
            .flatMapLatest { day -> dayPopulator.populateSingle(day, PopulationContext.Profile(profile)) }
    }
}