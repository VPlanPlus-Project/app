package plus.vplan.app.feature.settings.page.developer.logs

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

expect fun getLogs(): Flow<Log>
expect fun clearLogs()

class DeveloperLogsViewModel : ViewModel() {
    private val _state = MutableStateFlow(DeveloperLogsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getLogs().collect { logs ->
                _state.update {
                    val updatedLogs = it.logs + logs
                    it.copy(logs = updatedLogs.sortedByDescending { log -> log.timestamp })
                }
            }
        }
    }

    fun onEvent(event: DeveloperLogsEvent) {
        when (event) {
            is DeveloperLogsEvent.ClearLogs -> {
                viewModelScope.launch(Dispatchers.IO) {
                    clearLogs()
                    _state.update { it.copy(logs = emptyList())}
                }
            }
        }
    }
}

@Stable
data class DeveloperLogsState(
    val logs: List<Log> = emptyList()
)

sealed class DeveloperLogsEvent {
    object ClearLogs : DeveloperLogsEvent()
}

@Stable
data class Log(
    val timestamp: LocalDateTime,
    val tag: String,
    val level: LogLevel,
    val message: String,
) {
    enum class LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
    }

    val date = timestamp.format(LocalDateTime.Format {
        year(Padding.ZERO)
        char('-')
        monthNumber(Padding.ZERO)
        char('-')
        day(Padding.ZERO)
        char(' ')
        hour(Padding.ZERO)
        char(':')
        minute(Padding.ZERO)
        char(':')
        second(Padding.ZERO)
    })
}