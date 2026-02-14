package plus.vplan.app.core.model

internal object AliasPrefixConverter {
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

    fun toString(provider: AliasProvider): String {
        return provider.prefix
    }
}