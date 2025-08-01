package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasedItem
import kotlin.uuid.Uuid

data class Group(
    override val id: Uuid,
    val schoolId: Uuid,
    val name: String,
    val cachedAt: Instant,
    override val aliases: Set<Alias>
) : AliasedItem<DataTag> {
    override val tags: Set<DataTag> = emptySet()

    val school by lazy { App.schoolSource.getById(schoolId) }

    var schoolItem: School? = null
        private set

    suspend fun getSchoolItem(): School {
        return schoolItem ?: App.schoolSource.getById(schoolId).getFirstValue()!!.also { schoolItem = it }
    }
}