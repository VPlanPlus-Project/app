package plus.vplan.app.core.database.converters

import androidx.room.TypeConverter
import plus.vplan.app.core.model.CreationReason

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