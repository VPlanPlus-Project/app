package plus.vplan.app.core.model

import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLPath
import kotlin.uuid.Uuid

/**
 * Base interface for all items that can be cached, regardless of if this kind of entity would need
 * to be cached. If it originates from a network request or relates to something that is a result
 * of a network request, it should implement this interface.
 */
interface Item<ID, DT: DataTag> {
    val id: ID
    val tags: Set<DT>
}

/**
 * Interface for items that are owned by VPlanPlus and are identified by an integer ID. This is used
 * for entities that are created and managed within the VPlanPlus ecosystem, such as [plus.vplan.app.domain.model.Homework]
 * and [plus.vplan.app.domain.model.Assessment].
 */
interface VppItem<DT: DataTag> : Item<Int, DT>

/**
 * Interface used for items that may have aliases, where a UUID is used to identify the item in
 * the local database. See [plus.vplan.app.domain.repository.base.AliasedItemRepository.resolveAliasToLocalId]
 * for more information about the usage of such aliases. This is used for entities that aren't
 * exclusively owned by VPlanPlus. See [VppItem] for such items.
 */
interface AliasedItem<DT: DataTag> : Item<Uuid, DT> {
    val aliases: Set<Alias>
}

fun Set<Alias>.getByProvider(provider: AliasProvider): Alias? {
    return this.firstOrNull { it.provider == provider }
}

data class Alias(
    val provider: AliasProvider,
    val value: String,
    val version: Int
) {

    /**
     * Returns a string representation of the alias in the format:
     * `<provider>.<value>.<version>`.
     */
    override fun toString(): String {
        return "${provider.prefix}.${value}.$version"
    }

    fun toUrlString(): String {
        return "${provider.prefix}.${value.encodeURLPath(encodeSlash = true, encodeEncoded = true)}.$version"
    }

    companion object {
        val knownPrefixes = listOf("vpp", "sp24", "schulverwalter")
        private val SCHOOL_ALIAS_REGEX = Regex("(${knownPrefixes.joinToString("|")})\\.[^.]+\\.[0-9]+")

        fun fromString(value: String): Alias {
            if (!SCHOOL_ALIAS_REGEX.matches(value)) {
                throw IllegalArgumentException("Invalid school alias format. Expected format: <prefix>.<identifier>.<version>, or regex ${SCHOOL_ALIAS_REGEX.pattern}")
            }
            val (prefixValue, identifierValue, versionValue) = value.split(".", limit = 3)
            val prefix = AliasPrefixConverter.fromString(prefixValue)
            val identifier = identifierValue.decodeURLQueryComponent(plusIsSpace = false)
            val version = versionValue.toIntOrNull() ?: throw IllegalArgumentException("Invalid version number: $versionValue")
            return Alias(
                provider = prefix,
                value = identifier,
                version = version
            )
        }
    }
}

enum class AliasProvider(val prefix: String) {
    Sp24("sp24"),
    Vpp("vpp"),
    Schulverwalter("schulverwalter")
}

class InvalidAliasTypeException(tried: String, expected: List<String>) : IllegalArgumentException(
    "Tried to use alias type '$tried' but expected one of ${expected.joinToString(", ") { "'$it'" }}."
)