package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.AliasedItem
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Group(
    override val id: Uuid,
    val school: School.AppSchool,
    val name: String,
    val cachedAt: Instant,
    override val aliases: Set<Alias>
) : AliasedItem<DataTag> {
    override val tags: Set<DataTag> = emptySet()
    companion object {
        fun buildSp24Alias(sp24SchoolId: Int, groupName: String) = Alias(AliasProvider.Sp24, "$sp24SchoolId/$groupName", 1)
    }
}