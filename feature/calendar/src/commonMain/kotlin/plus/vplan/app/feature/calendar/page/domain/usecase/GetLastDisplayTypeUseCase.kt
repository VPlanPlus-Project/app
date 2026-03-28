package plus.vplan.app.feature.calendar.page.domain.usecase

import kotlinx.coroutines.flow.map
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.feature.calendar.page.domain.model.DisplayType

class GetLastDisplayTypeUseCase(
    private val keyValueRepository: KeyValueRepository,
    private val analyticsRepository: AnalyticsRepository,
) {
    operator fun invoke() = keyValueRepository.get(Keys.CALENDAR_DISPLAY_TYPE).map {
        if (it == null) return@map DisplayType.Calendar
        try {
            DisplayType.valueOf(it)
        } catch (e: IllegalArgumentException) {
            analyticsRepository.captureError("GetLastDisplayTypeUseCase", e.stackTraceToString())
            return@map DisplayType.Calendar
        }
    }
}