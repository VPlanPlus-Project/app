package plus.vplan.app.core.utils.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.math.abs
import kotlin.time.Instant

infix fun LocalDate.untilRelativeText(other: LocalDate): String? {
    val days = (other - this).days
    return when (days) {
        -2 -> "Vorgestern"
        -1 -> "Gestern"
        0 -> "Heute"
        1 -> "Morgen"
        2 -> "Übermorgen"
        else -> null
    }
}

const val SECONDS_PER_MINUTE = 60L
const val SECONDS_PER_HOUR = 3600L
const val SECONDS_PER_DAY = 86400L
const val JUST_NOW_THRESHOLD = 30L
infix fun Instant.untilRelativeText(other: Instant): String {
    val seconds = (other - this).inWholeSeconds
    val absSeconds = abs(seconds)
    val isFuture = seconds > 0

    val parts = mutableListOf<String>()

    val days = absSeconds / SECONDS_PER_DAY
    val hours = (absSeconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR
    val minutes = (absSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val secs = absSeconds % SECONDS_PER_MINUTE

    when {
        absSeconds < JUST_NOW_THRESHOLD -> return "Gerade eben"
        absSeconds < SECONDS_PER_DAY -> {
            if (hours > 0) parts += "$hours ${if (hours == 1L) "Stunde" else "Stunden"}"
            if (minutes > 0) parts += "$minutes ${if (minutes == 1L) "Minute" else "Minuten"}"
            if (parts.isEmpty()) parts += "$secs ${if (secs == 1L) "Sekunde" else "Sekunden"}"
        }
        else -> {
            if (days > 0) parts += "$days ${if (days == 1L) "Tag" else "Tagen"}"
            if (hours > 0) parts += "$hours ${if (hours == 1L) "Stunde" else "Stunden"}"
        }
    }

    val text = parts.joinToString(" und ")
    return if (isFuture) "in $text" else "vor $text"
}