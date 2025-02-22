package plus.vplan.app.domain.model.schulverwalter

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Item

data class Collection(
    val id: Int,
    val type: String,
    val name: String,
    val subjectId: Int,
    val intervalId: Int,
    val teacherId: Int,
    val cachedAt: Instant
): Item {
    override fun getEntityId(): String = this.id.toString()

    val interval by lazy { App.intervalSource.getById(intervalId) }
    val subject by lazy { App.subjectSource.getById(subjectId) }
}
