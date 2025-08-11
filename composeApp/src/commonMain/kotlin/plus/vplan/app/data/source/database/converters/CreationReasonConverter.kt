package plus.vplan.app.data.source.database.converters

import androidx.room.TypeConverter
import plus.vplan.app.domain.cache.CreationReason

class CreationReasonConverter {
    @TypeConverter
    fun fromCreationReason(value: CreationReason): String {
        return value.name
    }

    @TypeConverter
    fun toCreationReason(value: String): CreationReason {
        return CreationReason.valueOf(value)
    }
}