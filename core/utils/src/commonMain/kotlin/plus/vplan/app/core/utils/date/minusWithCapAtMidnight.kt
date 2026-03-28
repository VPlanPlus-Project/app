package plus.vplan.app.core.utils.date

import kotlinx.datetime.LocalTime
import kotlin.time.Duration

fun LocalTime.minusWithCapAtMidnight(duration: Duration): LocalTime {
    val seconds = this.toSecondOfDay()
    val newSeconds = (seconds - duration.inWholeSeconds).coerceAtLeast(0)
    return LocalTime.fromSecondOfDay(newSeconds.toInt())
}