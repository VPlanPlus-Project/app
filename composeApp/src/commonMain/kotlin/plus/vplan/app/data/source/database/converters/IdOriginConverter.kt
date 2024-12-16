package plus.vplan.app.data.source.database.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import plus.vplan.app.domain.model.EntityIdentifier

@ProvidedTypeConverter
class IdOriginConverter {

    @TypeConverter
    fun toIdOrigin(value: String): EntityIdentifier.Origin {
        return EntityIdentifier.Origin.valueOf(value)
    }

    @TypeConverter
    fun fromIdOrigin(origin: EntityIdentifier.Origin): String {
        return origin.name
    }
}