package plus.vplan.app.data.source.database.converters

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

object KotlinxInstantConverter {

    @TypeConverter
    fun toInstant(value: Long): Instant {
        return Instant.fromEpochMilliseconds(value)
    }

    @TypeConverter
    fun fromInstant(value: Instant): Long {
        return value.toEpochMilliseconds()
    }
}