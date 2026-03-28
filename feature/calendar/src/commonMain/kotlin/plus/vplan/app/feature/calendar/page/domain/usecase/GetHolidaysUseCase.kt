package plus.vplan.app.feature.calendar.page.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.holiday.HolidayRepository
import plus.vplan.app.core.model.Profile

class GetHolidaysUseCase(
    private val holidayRepository: HolidayRepository
) {
    operator fun invoke(profile: Profile): Flow<List<LocalDate>> {
        return holidayRepository.getBySchool(profile.school).map { it.map { holiday -> holiday.date } }
    }
}