package plus.vplan.app.feature.calendar.page.domain.usecase

import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.feature.calendar.page.domain.model.DisplayType

class SetLastDisplayTypeUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(type: DisplayType) {
        keyValueRepository.set(Keys.CALENDAR_DISPLAY_TYPE, type.name)
    }
}