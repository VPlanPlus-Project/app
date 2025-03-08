package plus.vplan.app.feature.calendar.domain.usecase

import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.feature.calendar.ui.DisplayType

class GetLastDisplayTypeUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    operator fun invoke() = keyValueRepository.get(Keys.CALENDAR_DISPLAY_TYPE).map {
        if (it == null) return@map DisplayType.Calendar
        try {
            DisplayType.valueOf(it)
        } catch (e: IllegalArgumentException) {
            return@map DisplayType.Calendar
        }
    }
}