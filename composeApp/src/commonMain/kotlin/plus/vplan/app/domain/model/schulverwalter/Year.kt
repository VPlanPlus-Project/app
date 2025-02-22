package plus.vplan.app.domain.model.schulverwalter

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.Item

data class Year(
    val id: Int,
    val name: String,
    val from: LocalDate,
    val to: LocalDate,
    val cachedAt: Instant
) : Item {
    override fun getEntityId(): String = this.id.toString()
}
