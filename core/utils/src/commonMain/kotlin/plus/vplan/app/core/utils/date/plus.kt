package plus.vplan.app.core.utils.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

operator fun LocalTime.plus(duration: Duration): LocalTime {
    val instant = atDate(LocalDate.fromEpochDays(0)).toInstant(TimeZone.UTC)
    return instant.plus(duration).toLocalDateTime(TimeZone.UTC).time
}