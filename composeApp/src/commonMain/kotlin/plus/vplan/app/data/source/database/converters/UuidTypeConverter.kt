package plus.vplan.app.data.source.database.converters

import androidx.room.TypeConverter
import kotlin.uuid.Uuid

class UuidTypeConverter {

    @TypeConverter
    fun toUuid(value: String): Uuid {
        return Uuid.parse(value)
    }

    @TypeConverter
    fun fromUuid(uuid: Uuid): String {
        return uuid.toString()
    }
}