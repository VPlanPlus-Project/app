@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.day.DayRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.School
import plus.vplan.app.domain.model.populated.DayPopulator
import plus.vplan.app.domain.model.populated.PopulatedDay
import plus.vplan.app.domain.model.populated.PopulationContext

class GetDayUseCase(
    private val dayRepository: DayRepository,
    private val dayPopulator: DayPopulator,
) {
    operator fun invoke(profile: Profile, date: LocalDate): Flow<PopulatedDay> {
        val school: School.AppSchool = profile.school
        return dayRepository.getBySchool(school, date)
            .flatMapLatest { day ->
                dayPopulator.populateSingle(day, PopulationContext.Profile(profile))
            }
            .flowOn(Dispatchers.Default)
    }
}