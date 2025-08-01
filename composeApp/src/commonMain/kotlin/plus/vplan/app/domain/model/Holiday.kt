package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item
import kotlin.uuid.Uuid

data class Holiday(
    val id: String,
    val date: LocalDate,
    val school: Uuid
): Item<DataTag> {
    constructor(date: LocalDate, school: Uuid) : this(id = "${school}/$date", date = date, school = school)

    override fun getEntityId(): String = this.id
    override val tags: Set<DataTag> = emptySet()
}