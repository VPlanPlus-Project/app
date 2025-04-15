package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue

data class Room(
    val id: Int,
    val schoolId: Int,
    val name: String,
    val cachedAt: Instant
) : Item<DataTag> {
    override fun getEntityId(): String = this.id.toString()

    var school: School? = null
        private set

    suspend fun getSchoolItem(): School {
        return school ?: App.schoolSource.getById(schoolId).getFirstValue()!!.also { school = it }
    }
}