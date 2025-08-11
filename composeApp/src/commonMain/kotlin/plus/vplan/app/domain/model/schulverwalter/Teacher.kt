package plus.vplan.app.domain.model.schulverwalter

import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item

data class Teacher(
    override val id: Int,
    val forename: String,
    val name: String,
    val localId: String,
    val cachedAt: Instant
): Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()
}
