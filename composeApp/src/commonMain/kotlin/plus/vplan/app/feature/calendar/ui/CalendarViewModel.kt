package plus.vplan.app.feature.calendar.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SchoolDay

class CalendarViewModel : ViewModel() {
    var state by mutableStateOf(CalendarState())
        private set

    fun onEvent(event: CalendarEvent) {
    }
}

data class CalendarState(
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val currentSelectCause: DateSelectCause = DateSelectCause.CALENDAR_CLICK,
    val currentProfile: Profile? = null,
    val days: Map<LocalDate, SchoolDay> = emptyMap(),
)

sealed class CalendarEvent {
    data class SelectDate(val date: LocalDate, val cause: DateSelectCause) : CalendarEvent()
}

enum class DateSelectCause {
    CALENDAR_SWIPE,
    CALENDAR_CLICK,
    DAY_SWIPE,
}