package plus.vplan.app.core.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Teacher(
    override val id: Uuid,
    val school: School.AppSchool,
    val name: String,
    val cachedAt: Instant,
    override val aliases: Set<Alias>
) : AliasedItem<DataTag> {
    override val tags: Set<DataTag> = emptySet()
}