package plus.vplan.app.domain.model.schulverwalter

import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item

data class FinalGrade(
    override val id: Int,
    val calculationRule: String,
    val subjectId: Int,
    val cachedAt: Instant
): Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()
}