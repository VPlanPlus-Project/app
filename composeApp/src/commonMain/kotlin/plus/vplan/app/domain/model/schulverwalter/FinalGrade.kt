package plus.vplan.app.domain.model.schulverwalter

import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.Item

data class FinalGrade(
    val id: Int,
    val calculationRule: String,
    val subjectId: Int,
    val cachedAt: Instant
): Item {
    override fun getEntityId(): String = this.id.toString()
}