package plus.vplan.app.feature.settings.page.developer.logs

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

data class DeveloperLogsState(
    val logs: List<Log> = emptyList()
)

sealed class DeveloperLogsEvent {
    object ClearLogs : DeveloperLogsEvent()
}

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
}