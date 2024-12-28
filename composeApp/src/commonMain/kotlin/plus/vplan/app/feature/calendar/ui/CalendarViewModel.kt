package plus.vplan.app.feature.calendar.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            when (event) {
                is CalendarEvent.SelectDate -> state = state.copy(selectedDate = event.date)
            }
        }
    }
}

data class CalendarState(
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val currentProfile: Profile? = null,
    val days: Map<LocalDate, SchoolDay> = emptyMap(),
)

sealed class CalendarEvent {
    data class SelectDate(val date: LocalDate) : CalendarEvent()
}