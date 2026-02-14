package plus.vplan.app.core.model

import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

data class Holiday(
    override val id: String,
    val date: LocalDate,
    val school: Uuid
): Item<String, DataTag> {
    constructor(date: LocalDate, school: Uuid) : this(id = "${school}/$date", date = date, school = school)

    override val tags: Set<DataTag> = emptySet()
}