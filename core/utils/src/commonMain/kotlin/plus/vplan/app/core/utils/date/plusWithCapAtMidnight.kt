package plus.vplan.app.core.utils.date

import kotlinx.datetime.LocalTime
import kotlin.time.Duration

fun LocalTime.plusWithCapAtMidnight(duration: Duration): LocalTime {
    val seconds = this.toSecondOfDay()
    val newSeconds = (seconds + duration.inWholeSeconds).coerceAtMost(24 * 60 * 60)
    return LocalTime.fromSecondOfDay(newSeconds.toInt())
}