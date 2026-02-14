@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.domain.model

import plus.vplan.app.App
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.DataTag
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.AliasedItem
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
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

    companion object {
        fun buildSp24Alias(sp24SchoolId: Int, groupName: String) = Alias(AliasProvider.Sp24, "$sp24SchoolId/$groupName", 1)
    }
}