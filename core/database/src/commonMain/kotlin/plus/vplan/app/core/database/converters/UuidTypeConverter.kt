package plus.vplan.app.core.database.converters

import androidx.room.TypeConverter
import kotlin.uuid.Uuid

class UuidTypeConverter {

    @TypeConverter
    fun toUuid(value: String): Uuid {
        return if (value.contains("-")) Uuid.parse(value) else Uuid.parseHex(value)
    }

    @TypeConverter
    fun fromUuid(uuid: Uuid): String {
        return uuid.toString()
    }
}