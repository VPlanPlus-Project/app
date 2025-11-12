package plus.vplan.app.feature.settings.page.developer.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

expect fun getLogs(): Flow<List<Log>>

class DeveloperLogsViewModel : ViewModel() {
    private val _state = MutableStateFlow(DeveloperLogsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getLogs().collect { logs ->
                _state.value = _state.value.copy(
                    logs = logs.sortedByDescending { it.timestamp }
                )
            }
        }
    }
}

data class DeveloperLogsState(
    val logs: List<Log> = emptyList()
)

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