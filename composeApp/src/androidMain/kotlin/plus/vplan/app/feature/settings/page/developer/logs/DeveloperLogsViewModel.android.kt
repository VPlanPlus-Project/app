package plus.vplan.app.feature.settings.page.developer.logs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

actual fun getLogs(): Flow<Log> {
    return flow {
        Runtime.getRuntime().exec("logcat")
            .inputStream
            .bufferedReader()
            .useLines { lines ->
                lines.forEach { line ->
                    // Example log line:
                    // 06-12 14:23:45.678  1234  5678  I TagName: This is a log message
                    val logRegex = Regex("""(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+([VDIWE])\s+(\S+):\s+(.*)""")
                    val matchResult = logRegex.find(line)
                    if (matchResult != null) {
                        val (timestampStr, _, _, levelChar, tag, message) = matchResult.destructured
                        val level = when (levelChar) {
                            "V" -> Log.LogLevel.VERBOSE
                            "D" -> Log.LogLevel.DEBUG
                            "I" -> Log.LogLevel.INFO
                            "W" -> Log.LogLevel.WARN
                            "E" -> Log.LogLevel.ERROR
                            else -> Log.LogLevel.DEBUG
                        }
                        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
                        val timestamp = LocalDateTime.parse("$currentYear-${timestampStr.substringBeforeLast(".")}", LocalDateTime.Format {
                            year(padding = Padding.ZERO)
                            char('-')
                            monthNumber(padding = Padding.ZERO)
                            char('-')
                            day(padding = Padding.ZERO)
                            char(' ')
                            hour(padding = Padding.ZERO)
                            char(':')
                            minute(padding = Padding.ZERO)
                            char(':')
                            second(padding = Padding.ZERO)
                        })
                        emit(Log(
                            timestamp = timestamp,
                            tag = tag,
                            level = level,
                            message = message
                        ))
                    }
                }
            }
    }
}

actual fun clearLogs() {
    val process = Runtime.getRuntime().exec("logcat -c")
    process.waitFor()
}