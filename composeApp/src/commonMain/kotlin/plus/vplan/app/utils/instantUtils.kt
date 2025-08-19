@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun Instant.toLocalDateTime(timeZone: TimeZone) = kotlinx.datetime.Instant.fromEpochMilliseconds(this.toEpochMilliseconds()).toLocalDateTime(timeZone)

fun LocalDateTime.toKotlinInstant(timeZone: TimeZone) = Instant.fromEpochMilliseconds(this.toInstant(timeZone).toEpochMilliseconds())