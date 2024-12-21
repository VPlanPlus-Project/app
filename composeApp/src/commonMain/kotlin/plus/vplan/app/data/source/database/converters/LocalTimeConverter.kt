package plus.vplan.app.data.source.database.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import kotlinx.datetime.LocalTime

@ProvidedTypeConverter
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