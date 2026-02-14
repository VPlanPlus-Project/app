package plus.vplan.app.core.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Group(
    override val id: Uuid,
    val schoolId: Uuid,
    val name: String,
    val cachedAt: Instant,
    override val aliases: Set<Alias>
) : AliasedItem<DataTag> {
    override val tags: Set<DataTag> = emptySet()

    companion object {
        fun buildSp24Alias(sp24SchoolId: Int, groupName: String) = Alias(AliasProvider.Sp24, "$sp24SchoolId/$groupName", 1)
    }
}