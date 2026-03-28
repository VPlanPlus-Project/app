@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.core.common.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.day.DayRepository
import plus.vplan.app.core.data.populator.DayPopulator
import plus.vplan.app.core.data.populator.PopulatedDay
import plus.vplan.app.core.data.populator.PopulationContext
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.School

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