package plus.vplan.app.core.database.converters

import androidx.room.TypeConverter
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.InvalidAliasTypeException

object AliasPrefixConverter {
    @TypeConverter
    fun fromString(value: String): AliasProvider {
        return when (value) {
            AliasProvider.Sp24.prefix -> AliasProvider.Sp24
            AliasProvider.Vpp.prefix -> AliasProvider.Vpp
            AliasProvider.Schulverwalter.prefix -> AliasProvider.Schulverwalter
            else -> throw InvalidAliasTypeException(
                tried = value,
                expected = AliasProvider.entries.map { it.prefix }
            )
        }
    }

    @TypeConverter
    fun toString(provider: AliasProvider): String {
        return provider.prefix
    }
}