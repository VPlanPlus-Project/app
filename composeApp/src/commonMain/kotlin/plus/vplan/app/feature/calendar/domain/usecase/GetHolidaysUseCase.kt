package plus.vplan.app.feature.calendar.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.DayRepository

class GetHolidaysUseCase(
    private val holidayRepository: DayRepository
) {
    suspend operator fun invoke(profile: Profile): Flow<List<LocalDate>> {
        return holidayRepository.getHolidays(profile.school.id).map { it.map { holiday -> holiday.date } }
    }
}