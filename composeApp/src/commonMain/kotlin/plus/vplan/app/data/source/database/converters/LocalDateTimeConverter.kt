package plus.vplan.app.data.source.database.converters

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime

class LocalDateTimeConverter {

    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime {
        return LocalDateTime.parse(value)
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime): String {
        return value.toString()
    }
}