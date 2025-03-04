package plus.vplan.app.feature.calendar.domain.usecase

import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.feature.calendar.ui.DisplayType

class SetLastDisplayTypeUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(type: DisplayType) {
        keyValueRepository.set(Keys.CALENDAR_DISPLAY_TYPE, type.name)
    }
}