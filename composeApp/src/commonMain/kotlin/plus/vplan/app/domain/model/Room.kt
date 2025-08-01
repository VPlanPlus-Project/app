package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasedItem
import kotlin.uuid.Uuid

data class Room(
    override val id: Uuid,
    val schoolId: Uuid,
    val name: String,
    val cachedAt: Instant,
    override val aliases: Set<Alias>
) : AliasedItem<DataTag> {
    override val tags: Set<DataTag> = emptySet()

    var school: School? = null
        private set

    suspend fun getSchoolItem(): School {
        return school ?: App.schoolSource.getById(schoolId).getFirstValue()!!.also { school = it }
    }
}