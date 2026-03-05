@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

operator fun LocalDateTime.plus(duration: Duration): LocalDateTime {
    val instant = toInstant(TimeZone.UTC)
    return instant.plus(duration).toLocalDateTime(TimeZone.UTC)
}

operator fun LocalDateTime.minus(duration: Duration): LocalDateTime {
    val instant = toInstant(TimeZone.UTC)
    return instant.minus(duration).toLocalDateTime(TimeZone.UTC)
}
