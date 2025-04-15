package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue

data class Group(
    val id: Int,
    val schoolId: Int,
    val name: String,
    val cachedAt: Instant
) : Item<DataTag> {
    override fun getEntityId(): String = id.toString()
    override val tags: Set<DataTag> = emptySet()

    val school by lazy { App.schoolSource.getById(schoolId) }

    var schoolItem: School? = null
        private set

    suspend fun getSchoolItem(): School {
        return schoolItem ?: App.schoolSource.getById(schoolId).getFirstValue()!!.also { schoolItem = it }
    }
}