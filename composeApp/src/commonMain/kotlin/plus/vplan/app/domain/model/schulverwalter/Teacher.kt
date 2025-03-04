package plus.vplan.app.domain.model.schulverwalter

import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.Item

data class Teacher(
    val id: Int,
    val forename: String,
    val name: String,
    val localId: String,
    val cachedAt: Instant
): Item {
    override fun getEntityId(): String = this.id.toString()
}
