package plus.vplan.app.feature.calendar.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.DayRepository

class GetHolidaysUseCase(
    private val holidayRepository: DayRepository
) {
    suspend operator fun invoke(profile: Profile): Flow<List<LocalDate>> {
        val school = profile.getSchool().getFirstValue() ?: return flowOf(emptyList())
        return holidayRepository.getHolidays(school.id).map { it.map { holiday -> holiday.date } }
    }
}