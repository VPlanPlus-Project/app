@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.domain.model.schulverwalter

import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class FinalGrade(
    override val id: Int,
    val calculationRule: String,
    val subjectId: Int,
    val cachedAt: Instant
): Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()
}