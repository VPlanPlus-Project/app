package plus.vplan.app.core.database.converters

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

class LocalDateConverter {
    @TypeConverter
    fun toLocalDate(value: String): LocalDate {
        return LocalDate.parse(value)
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate): String {
        return value.toString()
    }
}