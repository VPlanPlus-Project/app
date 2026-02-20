package plus.vplan.app.core.database.converters

import androidx.room.TypeConverter
import kotlin.time.Instant

object InstantConverter {

    @TypeConverter
    fun toInstant(value: Long): Instant {
        return Instant.fromEpochMilliseconds(value)
    }

    @TypeConverter
    fun fromInstant(value: Instant): Long {
        return value.toEpochMilliseconds()
    }
}