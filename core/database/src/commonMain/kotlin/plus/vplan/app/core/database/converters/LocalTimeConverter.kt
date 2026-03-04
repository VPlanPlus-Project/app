package plus.vplan.app.core.database.converters

import androidx.room.TypeConverter
import kotlinx.datetime.LocalTime

class LocalTimeConverter {
    @TypeConverter
    fun toLocalTime(value: String): LocalTime {
        return LocalTime.parse(value)
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime): String {
        return value.toString()
    }
}